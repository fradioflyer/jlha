/*
 * $RCSfile: StaticWriteHuffP.java,v $ $Date: 2001/11/19 12:56:35 $ 
 * Ver. $Name:  $  Source revision. $Revision: 1.2 $
 *
 * Copyright 2000 by TURNER.
 */

package jp.gr.java_conf.turner.util.lha;
import java.io.*;

/**
 * スライド辞書一致位置エンコード用静的ハフマン辞書.
 *
 * @author TURNER
 */
class StaticWriteHuffP extends StaticWriteHuff
{
	int codelen_max;

	/**
	 * スライド辞書一致位置エンコード用静的ハフマン辞書コンストラクタ.
	 *
	 * @param size ハフマン辞書の大きさ
	 */
	protected StaticWriteHuffP( int size ){
		super( size );
	}

	/**
	 * エンコード処理.
	 *
	 * @param cutter データを読み込むビットカッター（ストリーム）
	 */
	public void encode( int code, BitPacker packer )
		throws IOException
	{
		int len = getCodeLen( code );
		//* エンコードして書き出す
		super.encode( len, packer );
		if( encodeModeFlg && len > 1 ){
			packer.putBits( code & ((1 << (len-1)) - 1), (len-1) );
		}
	}

	/**
	 * 有効コード長を数える関数.
	 * 最大30ビットまで.
	 *
	 * @param code コード長を数える実際のコード
	 *
	 * @return 有効コード長
	 */
	private int getCodeLen( int code ){
		//* codeの有効ビット数を数える
		int len;
		for( len = 0; len <= 30; len++ ){
			if( (1 << len) > code ) break;
		}
		return len;
	}

}
