package es.tfg.codeguard.service.imp;

import es.tfg.codeguard.model.dto.CompilerResponseDTO;
import es.tfg.codeguard.model.dto.CompilerTestRequestDTO;
import es.tfg.codeguard.model.dto.SolutionDTO;
import es.tfg.codeguard.service.UserService;
import es.tfg.codeguard.service.ExerciseService;
import es.tfg.codeguard.service.JWTService;
import es.tfg.codeguard.util.CompilationErrorException;
import es.tfg.codeguard.util.NotAllowedUserException;
import es.tfg.codeguard.model.dto.CompilerRequestDTO;
import es.tfg.codeguard.model.repository.exercise.ExerciseRepository;
import es.tfg.codeguard.service.CompilerService;
import es.tfg.codeguard.util.PlaceholderNotFoundException;
import es.tfg.codeguard.util.TestCasesNotFoundException;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CompilerServiceImp implements CompilerService {

    @Autowired
    private JWTService jwtService;
    @Autowired
    private ExerciseService exerciseService;
    @Autowired
    private ExerciseRepository exerciseRepository;
    @Autowired
    private UserService userService;
    Logger logger = LoggerFactory.getLogger(CompilerServiceImp.class);

    @Override
    public CompilerResponseDTO compileSolution(String userToken, CompilerRequestDTO compileInfo) throws ClassNotFoundException, IOException, CompilationErrorException, TimeoutException, InterruptedException, TestCasesNotFoundException {
        Optional<String> tests = exerciseService.getTestFromExercise(compileInfo.exerciseId());
        if (tests.isEmpty()) {
            throw new TestCasesNotFoundException("You can't compile an spell without tests");
        }
        String javaCode = compileInfo.exerciseSolution();
        String testCode = tests.get();

        CompilerResponseDTO compilerResponse = compilator(userToken, javaCode, testCode);

        if (compilerResponse.exerciseCompilationCode() == 0 && compilerResponse.executionCode() == 0) {
            //Se guarda la solucion del usuario
            exerciseService.addSolutionToExercise(new SolutionDTO(compileInfo.exerciseId(),
                    jwtService.extractUserPass(userToken).getUsername(),
                    javaCode));
        }
        return compilerResponse;
    }

    public CompilerResponseDTO compileTest(String userToken, CompilerTestRequestDTO compileInfo) throws ClassNotFoundException, IOException, CompilationErrorException, TimeoutException, InterruptedException, TestCasesNotFoundException, PlaceholderNotFoundException {
        if (compileInfo.exerciseTests().isEmpty()) {
            throw new TestCasesNotFoundException("No test cases are given");
        }
        if (compileInfo.exercisePlaceHolder().isEmpty()) {
            throw new PlaceholderNotFoundException("No placeholder is given to the exercise");
        }
        String javaCode = compileInfo.exerciseSolution();
        String testCode = compileInfo.exerciseTests();
        String username = jwtService.extractUserPass(userToken).getUsername();

        if (!userService.getUserById(username).tester()) throw new NotAllowedUserException(username);

        CompilerResponseDTO compilerResponse = compilator(userToken, javaCode, testCode);

        if (compilerResponse.exerciseCompilationCode() == 0 && compilerResponse.executionCode() == 0) {
            exerciseService.addTestToExercise(new SolutionDTO(compileInfo.exerciseId(),
                            username,
                            javaCode),
                    testCode,
                    compileInfo.exercisePlaceHolder());
        }
        return compilerResponse;
    }

    private CompilerResponseDTO compilator(String userToken, String javaCode, String testCode) throws ClassNotFoundException, IOException, CompilationErrorException, TimeoutException, InterruptedException {

        Pattern javaClassPattern = Pattern.compile("(?:public)(?:\\s+)(?:class)(?:\\s+)(\\w+)");

        Matcher appClassNameMatcher = javaClassPattern.matcher(javaCode);
        String javaClassName = "";
        if (appClassNameMatcher.find()) {
            javaClassName = appClassNameMatcher.group(1);
        } else {
            throw new ClassNotFoundException("Could not find the class name inside the Java Code");
        }

        Matcher testClassNameMatcher = javaClassPattern.matcher(testCode);
        String testClassName = "";
        if (testClassNameMatcher.find()) {
            testClassName = testClassNameMatcher.group(1);
        } else {
            throw new ClassNotFoundException("Could not find the class name inside the Test Code");
        }

        String folderRoute = "src/main/resources/compilation/" + jwtService.extractUserPass(userToken).getUsername();

        File userFolder = new File(folderRoute);
        if (userFolder.exists()) {
            FileUtils.deleteDirectory(userFolder);
        }
        if (userFolder.mkdirs()) {
            logger.info("User folder " + userFolder.toString() + " created");
        } else {
            logger.error("User folder " + userFolder.toString() + " could not be created");
            throw new CompilationErrorException("Could not create the folder for compilation");
        }

        String javaFile = folderRoute + "/" + javaClassName + ".java";
        String testFile = folderRoute + "/" + testClassName + ".java";

        File javaJavaFile = new File(javaFile);
        File testJavaFile = new File(testFile);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(javaJavaFile))) {
            bw.write(javaCode);
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(testJavaFile))) {
            bw.write(testCode);
        }

        //Obtener los .jar para la compilación
        String userDir = System.getProperty("user.dir");
        String junitJupiterApiJar = Paths.get(userDir,"lib", "junit-jupiter-api-5.11.0.jar").toString();
        String junitPlatformConsoleJar = Paths.get(userDir,"lib", "junit-platform-console-standalone-1.11.3.jar").toString();

        //Compilation
        ProcessBuilder compilation = new ProcessBuilder("javac", "-Xlint:none", javaFile, testFile, "-cp", junitJupiterApiJar);
        compilation.redirectErrorStream(true);
        Process compilationProcess = compilation.start();

        StringBuilder compilationErrorMessage = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(compilationProcess.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                compilationErrorMessage.append("\n" + line);
            }
        }

        String compilationExitMessage = "Correct program compilation";
        int compilationExitCode = compilationProcess.waitFor();
        if (compilationExitCode != 0) {
            compilationExitMessage = "Compilation Error: " + compilationErrorMessage.toString();
            FileUtils.deleteDirectory(userFolder);
            return new CompilerResponseDTO(compilationExitCode, compilationExitMessage, null, null);
        }

        //JUnit 5 console execution
        ProcessBuilder testExecutor = new ProcessBuilder("java",
                "-jar",
                junitPlatformConsoleJar, //JUnit 5 console jar
                "execute", //Si no se usa "execute" la consola en la version 1.11.3 da un warning
                "-cp", //--class-path -cp
                ".",
                "-c", //--select-class -c
                testClassName,
                "--details=tree", //Modes: [flat, verbose, tree]
                "--disable-banner",
                "--disable-ansi-colors");
        testExecutor.redirectErrorStream(true);
        testExecutor.directory(userFolder);
        File executionOutput = new File(folderRoute + "/execution_output.txt");
        testExecutor.redirectOutput(executionOutput);
        Process testExecutorProcess = testExecutor.start();


        StringBuilder executionMessage = new StringBuilder();
        //Se esperan 15 segundos antes de destruir el proceso para evitar bucles infinitos
        if (!testExecutorProcess.waitFor(15, TimeUnit.SECONDS)) {
            testExecutorProcess.destroy();
            logger.info("Execution Timeout");
            testExecutorProcess.waitFor();
            //Necesary for the file deletion
            testExecutorProcess.getInputStream().close();
            testExecutorProcess.getOutputStream().close();
            testExecutorProcess.getErrorStream().close();
            FileUtils.deleteDirectory(userFolder);
            throw new TimeoutException("Exceeded the 15 seconds time limit");
        } else {
            try (BufferedReader br = new BufferedReader(new FileReader(executionOutput))) {
                String line;
                while ((line = br.readLine()) != null) {
                    executionMessage.append(line);
                    executionMessage.append("\n");
                }
            }
        }

        int executionExitCode = testExecutorProcess.exitValue();
        String executionExitMessage = filterConsoleOutput(executionMessage.toString());

        logger.info("Execution successfully");

        FileUtils.deleteDirectory(userFolder);
        return new CompilerResponseDTO(compilationExitCode, compilationExitMessage, executionExitCode, executionExitMessage);
    }

    private String filterConsoleOutput(String consoleOutput) {
        String[] lines = consoleOutput.split("\n");
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i != 0 && i != 1 && i != 5 && i != 6) {
                if (lines[i].startsWith("[") && lines[i].contains("containers")) {
                } else {
                    output.append(lines[i]);
                    output.append("\n");
                }
            }
        }
        return output.toString().replace("?", "");
    }
}
