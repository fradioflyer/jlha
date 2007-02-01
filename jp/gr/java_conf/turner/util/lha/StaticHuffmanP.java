/*
 * $RCSfile: StaticHuffmanP.java,v $ $Date: 2000/04/15 17:28:07 $ 
 * Ver. $Name:  $  Source revision. $Revision: 1.1.1.1 $
 *
 * Copyright 2000 by TURNER.
 */

package jp.gr.java_conf.turner.util.lha;
import java.io.*;

/**
 * スライド辞書一致位置デコード用静的ハフマン辞書.
 *
 * @author TURNER
 */
class StaticHuffmanP extends StaticHuffman
{

	/**
	 * スライド辞書一致位置デコード用静的ハフマン辞書.
	 *
	 * @param size ハフマン辞書の大きさ
	 */
	protected StaticHuffmanP( int size ){
		super( size );
	}

	/**
	 * デコード処理.
	 *
	 * @param cutter データを読み込むビットカッター（ストリーム）
	 */
	public int decode( BitCutter cutter )
		throws IOException
	{
		int ret = super.decode(cutter);

		if (ret != 0){
			ret = (1 << (ret - 1)) + cutter.getBits(ret - 1);
		}
		else{
			ret = 0;
		}
		return ret;
	}

}
