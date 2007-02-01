/*
 * $RCSfile: ItfHuffman.java,v $ $Date: 2000/04/15 17:28:07 $ 
 * Ver. $Name:  $  Source revision. $Revision: 1.1.1.1 $
 *
 * Copyright 2000 by TURNER.
 */

package jp.gr.java_conf.turner.util.lha;

import java.io.IOException;

interface ItfHuffman 
{
	public int decode( BitCutter cutter )
			throws IOException;

}