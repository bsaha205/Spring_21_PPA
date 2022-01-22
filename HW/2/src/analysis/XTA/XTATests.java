package analysis.XTA;

import org.junit.Test;

import junit.framework.TestCase;

/* A JUnit test that configures the argument array, then launches Soot and RTAAnalysis. 
 * For more information on basic Soot command-line arguments check this site:
 * https://github.com/Sable/soot/wiki/Introduction:-Soot-as-a-command-line-tool
*/

public class XTATests extends TestCase {
	
	// TODO: YOUR CODE HERE: Configure RT_HOME to point to the rt.jar on your machine.
	// I will be testing with Java 7 on Submitty, although this should not matter
	private static String RT_HOME = "/Users/bishwajit/Software/eclipse/rt.jar";
	
	@Test
	public void test2() {
		String[] args = new String[6];
		// -app causes Soot to run in "application mode", i.e., analysis scope is application 
		// classes only, no JDK classes. For now, consider this unsound application-only analysis. 
		// Later we will include java.* classes in the analysis. 
		args[0] = "-app";
		// -f J causes Soot to write out .jimple files. The default output directory is sootOutput
		// which is usually located in the same directory as src. Make sure you open .jimple files
		// and compare them the corresponding .java files
		args[1] = "-f";
		args[2] = "J";
		// -cp specifies the class path. Must include a path to the application classes, and the rt.jar
		args[3] = "-cp";
		args[4] = "./src/programs/p1/:"+RT_HOME;
		// specifies the class that contains the "main" method
		args[5] = "A";
 		XTA.main(args);
	}
	
}
