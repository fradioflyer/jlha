/*
 * $RCSfile: LZHEncoder.java,v $ $Date: 2001/11/23 12:25:59 $ 
 * Ver. $Name:  $  Source revision. $Revision: 1.2 $
 *
 * Copyright 2000 by TURNER.
 */

package jp.gr.java_conf.turner.util.lha;

import java.io.*;
import java.util.*;

class LZHEncoder extends LZHSlideDic {

	BitPacker packer;       //ビットカッター（入力ストリームから任意ビット取得）

	int now_pos = 0;        //スライド辞書の処理中の先頭位置.
	int match_pos = 0;      //スライド辞書の一致位置
	int match_len = 0;      //スライド辞書の一致長

	ItfWriteHuff	hufC;       //コード用ハフマン辞書
	ItfWriteHuff	hufP;       //スライド辞書ポインタ用ハフマン辞書

	private long count = 0;

	private short[] block = new short[32768/8*7];

	/**
	 * スライド辞書デコーダークラスのコンストラクタ.
	 * 
	 * @param dicbits 辞書の大きさ（ビット数）
	 * @param packer ビットカッター（任意のビットを切り出す入力ストリームのラッパー）
	 */
	protected LZHEncoder( int cmp_method, BitPacker packer )
	{
		super( cmp_method );
		block_size = 0;

		this.packer = packer;
	}

	/**
	 * 静的ハフマンテーブルを書き込む.
	 * 
	 * @exception java.io.IOException
	 */
	private void makeTreeAndSaveStaticHuffmanTable()
		throws IOException
	{
		StaticWriteHuffC tmp_hufC = (StaticWriteHuffC)hufC;
		StaticWriteHuffP tmp_hufP = (StaticWriteHuffP)hufP;
		tmp_hufC.makeTreeAndSaveTo( 9, -1, packer);
		tmp_hufP.makeTreeAndSaveTo( huf_p_bits , -1, packer);
	}

	/**
	 * 静的ハフマンテーブルを初期化する.
	 *
	 * @exception java.io.IOException
	 */
	private void initStaticHuffmanTable()
	{
		//ハフマンテーブルの生成
		hufC = new StaticWriteHuffC( 512 );
		hufP = new StaticWriteHuffP( huf_p_max );
	}

	/**
	 * 動的ハフマンテーブルを初期化する（lh1用）.
	 * 
	 */
	protected void initHuffmanTableForLH1()
	{
		throw new InternalError("this method unsupported now.");
		//hufC = new DynamicHuffmanC( 314 );
		//hufP = new ReadyMadeHuffmanP( huf_p_max );
	}

	/**
	 * データを展開しつつ１バイト取り出す.
	 * 
	 * @return 取り出した１バイトぶんのデータ
	 * @exception java.io.IOException
	 */
	protected void write( int c )
		throws IOException
	{
		count++;
		c &= 0xFF;
		if( block_size == 0 ){
			initStaticHuffmanTable();
		}

		block[block_size] = (short)(0xFF&c);
		hufC.encode( c, packer );


		//LH1以外のときはblock_sizeを更新する
		//block_sizeが満杯になったらフラッシュする
		if( block_size >= 0 ){
			block_size++;
			if( block_size == block.length ){
				flushBlock();
			}
		}
	}

	protected void flushBlock()
		throws IOException
	{
		if( block_size == 0 )return;

		packer.putBits( block_size >>> 8, 8 );
		packer.putBits( 0xFF&block_size, 8 );
		makeTreeAndSaveStaticHuffmanTable();

		for( int i=0; i<block_size; i++ ){
			hufC.encode( block[i], packer );
			if( block[i] >= 256 ){
				i++;
				hufP.encode( block[i], packer );
			}
		}
		block_size = 0;
	}
	
	protected long getCount(){
		return count;
	}

	private int search( int start ){
		return 0;
	}

}
