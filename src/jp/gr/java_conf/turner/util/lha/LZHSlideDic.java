/*
 * $RCSfile: LZHSlideDic.java,v $ $Date: 2001/11/23 09:51:27 $ 
 * Ver. $Name:  $  Source revision. $Revision: 1.1 $
 *
 * Copyright 2000 by TURNER.
 */

package jp.gr.java_conf.turner.util.lha;

import java.util.*;

abstract class LZHSlideDic {
	static final int THRESHOLD = 3; //一致長の最小サイズ

	byte[] dic;     //スライド辞書
	int dic_mask;   //スライド辞書インデックスのラップアラウンド用マスク
	
	int block_size = 0;     //ブロックサイズ（LH4～でLH7で使用）
	int huf_p_bits = 0;     //スライド辞書ポインタ用ハフマン辞書、有効サイズ読み込みビット数
	int huf_p_max = 0;      //スライド辞書ポインタ用ハフマン辞書、最大サイズ

	int cmp_method;         //圧縮メソッド


	/**
	 * スライド辞書デコーダークラスのコンストラクタ.
	 * 
	 * @param cmp_method 辞書の大きさ（ビット数）
	 */
	protected LZHSlideDic( int cmp_method )
	{
		int dicbits;
		this.cmp_method = cmp_method;
		switch( cmp_method ){
		case LhaInputStream.CMP_TYPE_LH1:
			dicbits = 12;		//４Ｋバイト
			//LH1ではハフマンテーブルをファイルから読み込まないので
			//huf_p_bitsの値は使用しない.
			huf_p_max = 1 << (12 - 6);
			initHuffmanTableForLH1();
			block_size = -1;//lh1にはブロックがないので無効を表す-1
			break;
		case LhaInputStream.CMP_TYPE_LH4:
			dicbits = 12;		//４Ｋバイト
			huf_p_bits = 4;
			huf_p_max = 14;
			block_size = 0;
			break;
		case LhaInputStream.CMP_TYPE_LH5:
			dicbits = 13;		//８Ｋバイト
			huf_p_bits = 4;
			huf_p_max = 14;
			block_size = 0;
			break;
		case LhaInputStream.CMP_TYPE_LH6:
/*
*			UNLHA32.DLLのヘルプによると
*			LH6のヘッダでLH7で圧縮されたファイルも存在するらしい.
*
*			dicbits = 15;		//３２Ｋバイト
*			huf_p_bits = 5;
*			huf_p_max = 16;
*			block_size = 0;
*			break;
*/
		case LhaInputStream.CMP_TYPE_LH7:
			dicbits = 16;		//６４Ｋバイト
			huf_p_bits = 5;
			huf_p_max = 32;
			block_size = 0;
			break;
		default:
			throw new Error("internal Error Illegal method:"+cmp_method);
		}

		int dicsize = 1 << dicbits;
		this.dic = new byte[dicsize];
		dic_mask = dicsize - 1;
		Arrays.fill( dic, (byte)' ' );
	}

	static protected String toHex( long n, int len ){
		String hex = "0000000000000000"+Long.toHexString(n);
		hex = hex.toUpperCase();
		while( Math.pow( 16 , len ) <= n ){
			len++;
		}
		return hex.substring( hex.length() - len );
	}

	abstract protected void initHuffmanTableForLH1();
}
