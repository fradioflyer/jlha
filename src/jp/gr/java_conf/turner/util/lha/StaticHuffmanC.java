/*
 * $RCSfile: StaticHuffmanC.java,v $ $Date: 2001/11/21 17:09:00 $ 
 * Ver. $Name:  $  Source revision. $Revision: 1.5 $
 *
 * Copyright 2000 by TURNER.
 */

package jp.gr.java_conf.turner.util.lha;

import java.io.*;


/**
 * コード用静的ハフマン辞書.
 * 
 * @author TURNER
 */
class StaticHuffmanC extends StaticHuffman
{
	/**
	 * コード用静的ハフマン辞書のコンストラクタ.
	 * 
	 * @param table_size ハフマン辞書の大きさ。
	 */
	protected StaticHuffmanC( int table_size )
	{
		super( table_size );
	}

//	/**
//	 * コード用静的ハフマン辞書のコンストラクタをファイルから読み込む.
//	 * 
//	 * @param effective_len_bits ハフマン辞書の大きを読み込むビット数
//	 * @param huffman   このハフマン辞書を読み込むのに使用するハフマン辞書
//	 * @param cutter    読み込みに使用するビットカッター（ストリーム）
//	 */
//	protected void loadFrom( int effective_len_bits, int special_index,
//			BitCutter cutter )
//		throws IOException
//	{
///		readTableLen( effective_len_bits, special_index, cutter );
//
//		//コードが１つしかないテーブルの場合
//		//このとき、ハフマンコードの割り振りはいらない。
//		if( leafs.length > 1 ){
//			makeTableCode();
//		}
//		restoreTree();
//	}

/*
	protected void loadFrom(  int effective_len_bits, int special_index,
			BitCutter cutter )
		throws IOException
	{
		throw new InternalError("Not use this method in this class.");
	}
*/

	/**
	 * ハフマンコードのビット数を読み込む.
	 * 
	 * @param effective_len_bits ハフマン辞書の大きさを読み込むためのビット数
	 * @param huffman    このハフマン辞書を読み込むのに使用するハフマン辞書
	 * @param cutter     読み込みに使用するビットカッター（ストリーム）
	 */
	protected void readTableLen( int effective_len_bits, int special_index,
			BitCutter cutter )
		throws IOException
	{
		int TBIT = 5;		/* smallest integer such that (1 << TBIT) > * NT */
		int NT = 16+3;
		StaticHuffman huffman = new StaticHuffman(NT);
		huffman.loadFrom( TBIT, 3, cutter );

		int i, c;
		int effective_len = cutter.copyBits( effective_len_bits );

		if( effective_len == 0 ){
			super.readTableLen( effective_len_bits, 0, cutter );
		}
		else{
			cutter.skipBits( effective_len_bits );	

			leafs = new Leaf[effective_len];
			for( i=0; i < leafs.length; i++ ){
				leafs[i] = new Leaf();
			}

			i = 0;
			while( i < effective_len ){
				c = huffman.decode( cutter );
				if( c <= 2 ){
					switch( c ){
					case 0:
						c = 1;
						break;
					case 1:
						c = cutter.getBits(4) + 3;
						break;
					case 2:
						c = cutter.getBits( effective_len_bits ) + 20;
					}
					while( --c >= 0 ){
						leafs[i++].code_len = 0;
					}
				}
				else{
					leafs[i++].code_len = c - 2;
				}
			}
		}
	}
}
