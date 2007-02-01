/*
 * $RCSfile: StaticWriteHuffC.java,v $ $Date: 2001/11/21 17:08:58 $ 
 * Ver. $Name:  $  Source revision. $Revision: 1.5 $
 *
 * Copyright 2000 by TURNER.
 */

package jp.gr.java_conf.turner.util.lha;

import java.io.*;


/**
 * コード用静的ハフマン辞書書き込み用.
 * 
 * @author TURNER
 */
class StaticWriteHuffC extends StaticWriteHuff
{
	/**
	 * コード用静的ハフマン辞書のコンストラクタ.
	 * 
	 * @param table_size ハフマン辞書の大きさ。
	 */
	protected StaticWriteHuffC( int table_size )
	{
		super( table_size );
	}

//	/**
//	 * 集計情報を元にハフマンツリーを構築しファイルに書き込む.
//	 *
//	 * @param effective_len_bits 有効符号数の読み込みビット数
//	 * @param writeHuf           空白インデックス指定
//	 * @param packer             ビットパッカー（ストリーム）
//	 *
//	 * @param IOException IOエラーが起こったとき
//	 */
//	protected void makeTreeAndSaveTo(  int effective_len_bits, int special_index,
//			BitPacker packer )
//		throws IOException
//	{
//		Leaf[] sort = makeProvisionalTree();
//		makeCodeLen( sort );
//
//		//コードが１つしかないテーブルの場合
//		//ハフマンコードの割り振りはいらない。
//		if( leafs.length > 1 ){
//			makeTableCode();
//		}
//		restoreTree();
//
//
//	//	encodeTable = new Leaf[table_size];
//	//	for( int i=0; i < leafs.length; i++ ){
//	//		encodeTable[leafs[i].real_code] = leafs[i];
//	//	}
//
//		setEncodeMode();//** これ以降はエンコードモード
//	}

	/**
	 * ハフマンコードのビット数を書き込む.
	 * 
	 * @param effective_len_bits ハフマン辞書の大きさを読み込むためのビット数
	 * @param special_index      この関数では不使用
	 * @param cutter             読み込みに使用するビットカッター（ストリーム）
	 */
	protected void writeTableLen( int effective_len_bits, int special_index,
			BitPacker packer )
		throws IOException
	{
		if( leafs.length == 0 ){
			throw new InternalError( "no freq data." );
		}

		int NT = 16+3;
		int TBIT = 5;		/* smallest integer such that (1 << TBIT) > * NT */
		StaticWriteHuff writeHuf = new StaticWriteHuff(NT);

		if( leafs.length == 1 ){
			writeHuf.encode( 0, null );
			writeHuf.makeTreeAndSaveTo( TBIT, 3, packer );
			super.writeTableLen( effective_len_bits, 0, packer );
		}else{

			/* ハフマンテーブルの初期化のために頻度を数える */
			OutputStream dmy = new DmyOutputStream();
			writeTableLenSub( effective_len_bits, writeHuf, new BitPacker( dmy ) );

			/* 書き込む本番 */
			writeHuf.makeTreeAndSaveTo( TBIT, 3, packer );
			writeTableLenSub( effective_len_bits, writeHuf, packer );
		}
	}

	/**
	 * ハフマンコードのビット数を書き込む.
	 * 
	 * @param effective_len_bits ハフマン辞書の大きさを読み込むためのビット数
	 * @param writehuff          このハフマン辞書を読み込むのに使用するハフマン辞書
	 * @param cutter             読み込みに使用するビットカッター（ストリーム）
	 */
	private void writeTableLenSub( int effective_len_bits, StaticWriteHuff writehuf,
			BitPacker packer )
		throws IOException
	{
		packer.putBits( leafs.length, effective_len_bits );

		int i = 0;
		while( i < leafs.length ){

			int code_len = leafs[i++].code_len;
			if( code_len == 0 ){

				/* コードが空白０の時の処理 */

				/* 空白の連続する数を数える */
				int zero_cnt = 1;
				while( i < leafs.length && leafs[i].code_len == 0 ){
					i++;
					zero_cnt++;
				}

				if( zero_cnt <= 2 ){

					/* 空白が２個までのとき */
					for( int j = 0; j < zero_cnt; j++ ){
						writehuf.encode( 0, packer );
					}
				}else if ( zero_cnt <= 18 /* 2^4+3 */ ){

					/* 空白が18個までのとき */
					writehuf.encode( 1, packer );
					packer.putBits( zero_cnt - 3, 4 );
				}else if ( zero_cnt == 19 ){

					/* 空白が19個のときは１個のときの処理＋18個までの処理 */
					writehuf.encode( 0, packer );

					writehuf.encode( 1, packer );
					packer.putBits( 15 /* 18 - 3 */, 4 );
				}else{

					/* 空白が20個以上のときの処理 */
					writehuf.encode( 2, packer );
					packer.putBits( zero_cnt - 20, effective_len_bits );
				}
			}else{

				/* 空白以外の有効コードの時の処理 */
				writehuf.encode( code_len + 2, packer );
			}
		}
	}
}

/**
 * ダミー出力ストリーム.
 * 
 * @author TURNER
 */
class DmyOutputStream extends OutputStream{
	public void write( int c ){};
}
