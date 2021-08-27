package org.gepard.client;

// represents one substitutin matrix file (name, filename and whether 
// its a matrix for nucleotide sequences

public class SubstMatrixFile {
	
	private String m_name;
	private String m_file;
	private boolean m_isNucleotideMatrix;
	
	// constructor for quick attribute setting
	public SubstMatrixFile(String name, String file, boolean isNucleotideMatrix) {
		this.m_name = name;
		this.m_file = file;		
		this.m_isNucleotideMatrix = isNucleotideMatrix;
	}
	
	public String getName() {
		return m_name;
	}
	
	public String getFile() {
		return m_file;
	}
	
	public boolean isNucleotideMatrix() {
		return m_isNucleotideMatrix;
	}
}
