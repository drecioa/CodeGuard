package es.tfg.codeguard.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import es.tfg.codeguard.model.entity.exercise.Exercise;
import es.tfg.codeguard.model.entity.user.User;
import es.tfg.codeguard.model.entity.userpass.UserPass;
import es.tfg.codeguard.model.repository.exercise.ExerciseRepository;
import es.tfg.codeguard.model.repository.user.UserRepository;
import es.tfg.codeguard.model.repository.userpass.UserPassRepository;

@Component
public class DataIntializer {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserPassRepository userPassRepository;
    @Autowired
    private ExerciseRepository exerciseRepository;

    @Bean
    void firstAdmin() {
        User firstAdmin = new User("Saruman", true, true);
        UserPass firstAdminPass = new UserPass("Saruman",
                passwordEncoder.encode("s4rum4n"),
                true);

        userRepository.save(firstAdmin);
        userPassRepository.save(firstAdminPass);
    }

    @Bean
    void firstExercise() {
        Exercise firstExercise = new Exercise("reverse-words", "Reverse Words", "Complete the solution so that it reverses all of the words within the string passed in.\n" +
                "\n" +
                "Words are separated by exactly one space and there are no leading or trailing spaces.");
        firstExercise.setCreator("Saruman"); firstExercise.setTester("Merlin");
        firstExercise.setTest("import org.junit.jupiter.api.Test;\n" +
                "import static org.junit.jupiter.api.Assertions.assertEquals;\n" +
                "import java.util.Arrays;\n" +
                "import java.util.Collections;\n" +
                "import java.util.concurrent.ThreadLocalRandom;\n" +
                "import java.util.stream.IntStream;\n" +
                "import java.util.stream.Collectors;\n" +
                "\n" +
                "public class SolutionTest {\n" +
                "    @Test\n" +
                "    void fixedTests() {\n" +
                "        assertEquals(\"world! hello\", ReverseWords.reverseWords(\"hello world!\"));\n" +
                "        assertEquals(\"this like speak doesn't yoda\", ReverseWords.reverseWords(\"yoda doesn't speak like this\"));\n" +
                "        assertEquals(\"foobar\", ReverseWords.reverseWords(\"foobar\"));\n" +
                "        assertEquals(\"editor kata\", ReverseWords.reverseWords(\"kata editor\"));\n" +
                "        assertEquals(\"boat your row row row\", ReverseWords.reverseWords(\"row row row your boat\"));\n" +
                "        assertEquals(\"\", ReverseWords.reverseWords(\"\"));\n" +
                "    }\n" +
                "  \n" +
                "    @Test\n" +
                "    void randomTests() {\n" +
                "        ThreadLocalRandom rnd = ThreadLocalRandom.current();\n" +
                "        for(int run = 0; run < 40; ++run) {\n" +
                "            String input = IntStream.range(0, rnd.nextInt(12)).mapToObj(i -> rnd.ints(rnd.nextInt(1,12), 'a', 'z'+1).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)).collect(Collectors.joining(\" \"));\n" +
                "            String[] words = input.split(\" \");\n" +
                "            Collections.reverse(Arrays.asList(words));\n" +
                "            String expected = String.join(\" \", words);\n" +
                "            assertEquals(expected, ReverseWords.reverseWords(input), String.format(\"For input \\\"%s\\\"\", input));\n" +
                "        }\n" +
                "    }\n" +
                "}");
        firstExercise.setPlaceholder("public class ReverseWords{\n" +
                "\n" +
                " public static String reverseWords(String str){\n" +
                "     return false;\n" +
                " }\n" +
                "}");
        exerciseRepository.save(firstExercise);
    }

    @Bean
    void secondExercise() {
        Exercise secondExercise = new Exercise("square-every-digit", "Square Every Digit", "Welcome. In this exercise, you are asked to square every digit of a number and concatenate them.\n" +
                "For example, if we run 9119 through the function, 811181 will come out, because 92 is 81 and 12 is 1. (81-1-1-81)\n" +
                "Example #2: An input of 765 will/should return 493625 because 72 is 49, 62 is 36, and 52 is 25. (49-36-25)\n" +
                "Note: The function accepts an integer and returns an integer.\n" +
                "Happy Coding!");
        secondExercise.setCreator("Saruman"); secondExercise.setTester("Merlin");
        secondExercise.setTest("import org.junit.jupiter.api.*;import static org.junit.jupiter.api.Assertions.*;import java.util.Random;public class SquareDigitTest {    private void assertSolution(int expected, int n) {            int actual = assertDoesNotThrow(() -> new SquareDigit().squareDigits(n), \"Unexpected exception thrown by user solution for n=\" + n);      assertEquals(expected, actual, \"Incorrect answer for n=\" + n);    }    @Test    public void test() {      assertSolution(811181, 9119);      assertSolution(9414, 3212);      assertSolution(4114, 2112);      assertSolution(0, 0);    }    @Test    public void randomTest() {      Random random = new Random();      for (int i = 0; i < 100; i++) {        int test = 0;        test += random.nextInt(10);        test += random.nextInt(10) * 10;        test += random.nextInt(10) * 10 * 10;        test += random.nextInt(10) * 10 * 10 * 10;        assertSolution(squareDigits(test), test);              }    }    private int squareDigits(int n) {      String strDigits = String.valueOf(n);          String result = \"\";      for (char c : strDigits.toCharArray()) {        int digit = Character.digit(c, 10);        result += digit * digit;      }          return Integer.parseInt(result);    }}");
        secondExercise.setPlaceholder("public class SquareDigit {  public int squareDigits(int n) {    return 0; // TODO Implement me  }}");
        exerciseRepository.save(secondExercise);
    }

    @Bean
    void dataInizializerForFrontTesting() {
        User userTester = new User("Merlin", true, false);
        UserPass userTesterPass = new UserPass("Merlin",
                passwordEncoder.encode("merlin"),
                false);
        userRepository.save(userTester);
        userPassRepository.save(userTesterPass);
        User userCreator = new User("Gandalf", false, true);
        UserPass userCreatorPass = new UserPass("Gandalf",
                passwordEncoder.encode("gandalf"),
                false);
        userRepository.save(userCreator);
        userPassRepository.save(userCreatorPass);
        User userNormal = new User("Dumbledore", false, false);
        UserPass userNormalPass = new UserPass("Dumbledore",
                passwordEncoder.encode("dumbledore"),
                false);
        userRepository.save(userNormal);
        userPassRepository.save(userNormalPass);
        User userTryhard = new User("Harry", false, false);
        userTryhard.setExercises(new java.util.ArrayList<>(){{ add("reverse-words"); add("square-every-digit");}});
        UserPass userTryhardPass = new UserPass("Harry",
                passwordEncoder.encode("potter"),
                false);
        userRepository.save(userTryhard);
        userPassRepository.save(userTryhardPass);
    }

    @Bean
    void compileExercise() {
        Exercise thirdExercise = new Exercise("plural", "Plural",
                "We need a simple function that determines if a plural is needed or not. It should take a number, and return true if a plural should be used with that number or false if not. This would be useful when printing out a string such as 5 minutes, 14 apples, or 1 sun.\n" +
                        "\n" +
                        "You only need to worry about english grammar rules for this kata, where anything that isn't singular (one of something), it is plural (not one of something).\n" +
                        "\n" +
                        "All values will be positive integers or floats, or zero.");
        thirdExercise.setCreator("Saruman");
        thirdExercise.setTester("Saruman");
        thirdExercise.setTest("" +
                "import org.junit.jupiter.api.Test;\n" +
                "import static org.junit.jupiter.api.Assertions.assertEquals;\n" +
                "\n" +
                "\n" +
                "public class PluralTest {\n" +
                "    @Test\n" +
                "    public void BasicTest() {\n" +
                "      assertEquals(true,Plural.isPlural(0f));\n" +
                "      assertEquals(true,Plural.isPlural(0.5f));\n" +
                "      assertEquals(false,Plural.isPlural(1f));\n" +
                "      assertEquals(true,Plural.isPlural(100f));\n" +
                "    }\n" +
                "}");
        thirdExercise.setPlaceholder("public class Plural{ public static boolean isPlural(float f){}}");
        exerciseRepository.save(thirdExercise);
    }

}