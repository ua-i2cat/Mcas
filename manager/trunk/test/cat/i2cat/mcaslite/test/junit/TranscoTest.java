package cat.i2cat.mcaslite.test.junit;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cat.i2cat.mcaslite.entities.Transco;

public class TranscoTest {

	@Test
	public void transcoEqualsTest(){
		Transco transcoEq1 = new Transco("thisIsCommand", "thisIsOutput", "thisUriUnuset", "thisIsInput");
		Transco transcoEq2 = new Transco("thisIsCommand", "thisIsOutput", "adsfadsfda", "thisIsInput");
		Transco transcoEq3 = new Transco("thisIsCommand", "thisIsOutput", "thisUriUnuset", "asdfasdfasdfasdfas");
		Transco transcoEq4 = new Transco("sdfgsdg", "thisIsOutput", "thisUriUnuset", "thisIsInput");
		Transco transcoDiff = new Transco("asdfdasfasd", "sdfasdf", "thisUriUnuset", "thisIsInput");
		assertTrue(transcoEq1.equals(transcoEq2));
		assertTrue(transcoEq2.equals(transcoEq1));
		assertTrue(transcoEq3.equals(transcoEq4));
		assertTrue(transcoEq4.equals(transcoEq3));
		assertTrue(transcoEq1.equals(transcoEq4));
		assertTrue(! transcoDiff.equals(transcoEq4));
		assertTrue(! transcoEq4.equals(transcoDiff));
	}
}
