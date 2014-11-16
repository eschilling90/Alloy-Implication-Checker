import static org.junit.Assert.*;

import org.junit.Test;


public class FormatterTester {

	/*Started making test cases but for some reason, I can't
	 *run any of them. There is a null pointer exception that
	 *is thrown when I try to run the test case.
	 *The problem is in the junit launcher and not my stuff
	 *so I don't know what to fix. Maybe you can run it.
	*/
	@Test
	public void test() {
		Formatter f = new Formatter();
		assertEquals(f.trimComments("//comment"), "");
	}

}
