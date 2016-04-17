package it.quartara.boser.console.pdfcmgr;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import it.quartara.boser.console.HomeServletTest;

@RunWith(Suite.class)
@SuiteClasses({ PDFCManagerHelperTest.class, PDFCManagerJobTest.class,
		HomeServletTest.class })
public class AllTests {

}
