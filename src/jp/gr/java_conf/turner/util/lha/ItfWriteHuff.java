/*
 * $RCSfile: ItfWriteHuff.java,v $ $Date: 2001/11/16 17:28:01 $ 
 * Ver. $Name:  $  Source revision. $Revision: 1.1 $
 *
 * Copyright 2000 by TURNER.
 */

package jp.gr.java_conf.turner.util.lha;

import java.io.IOException;

interface ItfWriteHuff 
{
	public void encode( int code, BitPacker packer )
			throws IOException;

}
