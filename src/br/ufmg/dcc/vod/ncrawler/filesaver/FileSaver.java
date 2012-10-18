package br.ufmg.dcc.vod.ncrawler.filesaver;

/**
 * Interface for the file saver. Implementations of this class are responsible
 * for saving crawled file to disk.
 *  
 * @author Flavio Figueiredo - flaviovdf 'at' gmail.com 
 */
public interface FileSaver {

	/**
	 * Save the file with the given id and content
	 * 
	 * @param fileID ID of the file to save
	 * @param payload Content of the file
	 */
	public void save(String fileID, byte[] payload);
	
}
