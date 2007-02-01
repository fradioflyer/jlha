/*
 * $RCSfile: LZHDecoder.java,v $ $Date: 2001/11/23 09:51:28 $ 
 * Ver. $Name:  $  Source revision. $Revision: 1.5 $
 *
 * Copyright 2000 by TURNER.
 */

package jp.gr.java_conf.turner.util.lha;

import java.io.IOException;

class LZHDecoder extends LZHSlideDic {

	
	BitCutter cutter;       //ビットカッター（入力ストリームから任意ビット取得）

	int now_pos = 0;        //スライド辞書の処理中の先頭位置.
	int match_pos = 0;      //スライド辞書の一致位置
	int match_len = 0;      //スライド辞書の一致長

	ItfHuffman	hufC;       //コード用ハフマン辞書
	ItfHuffman	hufP;       //スライド辞書ポインタ用ハフマン辞書

	private long count = 0;

	/**
	 * スライド辞書デコーダークラスのコンストラクタ.
	 * 
	 * @param cmp_method 辞書の大きさ（ビット数）
	 * @param cutter ビットカッター（任意のビットを切り出す入力ストリームのラッパー）
	 */
	protected LZHDecoder( int cmp_method, BitCutter cutter )
	{
		super( cmp_method );
		this.cutter = cutter;
	}

	/**
	 * 静的ハフマンテーブルを読み込む.
	 * 
	 * @exception java.io.IOException
	 */
	private void loadStaticHuffmanTable()
		throws IOException
	{
		//int NT = 16+3;
		//int PBIT = 5;		/* smallest integer such that (1 << PBIT) > * NP */
		//int TBIT = 5;		/* smallest integer such that (1 << TBIT) > * NT */
		//ハフマンテーブルの生成
		//StaticHuffman huf = new StaticHuffman(NT);
		StaticHuffmanC tmp_hufC = new StaticHuffmanC( 512 );
		StaticHuffmanP tmp_hufP = new StaticHuffmanP( huf_p_max );

		//BitCutterからハフマンテーブルを読み込む.
		//huf.loadFrom( TBIT, 3, cutter );
		//tmp_hufC.loadFrom( 9, huf, cutter);
		tmp_hufC.loadFrom( 9, -1, cutter);
		tmp_hufP.loadFrom( huf_p_bits , -1, cutter);

		hufC = tmp_hufC;
		hufP = tmp_hufP;

	}

	/**
	 * 動的ハフマンテーブルを初期化する（lh1用）.
	 * 
	 */
	protected void initHuffmanTableForLH1()
	{
		hufC = new DynamicHuffmanC( 314 );
		hufP = new ReadyMadeHuffmanP( huf_p_max );
	}

	/**
	 * データを展開しつつ１バイト取り出す.
	 * 
	 * @return 取り出した１バイトぶんのデータ
	 * @exception java.io.IOException
	 */
	protected int read()
		throws IOException
	{
		int c = -1;
		
		if( match_len == 0 ){
			
			if( block_size > 0 ){
				block_size--;
			}else{
				if( block_size == 0 ){
					//ブロックサイズの読み込み.
					block_size = ( ((0xFF)&cutter.getBits(8)) << 8 );
					block_size |= (0xFF)&cutter.getBits(8);
					//このブロック用のハフマンテーブルの読み込み.
					loadStaticHuffmanTable();
					block_size--;
				}
			}

			c = 0x1FF & hufC.decode(cutter);
			if( (c & 0x100) == 0 ){

				dic[now_pos++] = (byte)c;	//辞書に登録
				now_pos &= dic_mask;	//posを辞書内に制限

				
				count++;
			}
			else{
				//一致長を取り出す
  				match_len = (c & 0x0FF) + THRESHOLD;
					
				//一致場所を取り出す
				int match_pos_wk = hufP.decode(cutter);
				match_pos = (now_pos - match_pos_wk - 1) & dic_mask;

				count++;
			}
		}
		if( match_len > 0 ){
			c = (0xFF & dic[match_pos]);

			match_pos++;
			match_pos &= dic_mask;

			dic[now_pos] = (byte)c;
			now_pos++;
			now_pos &= dic_mask;
			match_len--;

			count++;
		}
		return c;
	}
	
	/**
	 * データを展開しつつ配列に取り出す.
	 * 
	 * @param b    データを読み込むバイトの配列
	 * @param off  データの読み込み開始位置
	 * @param len  データを読み込む長さ
	 * @return 取り出した１バイトぶんのデータ
	 * @exception java.io.IOException
	 */
	protected void read( byte[] b, int off, int len )
		throws IOException
	{
		int c = -1;
		int tail = off + len;
		
		do{
			while( match_len == 0 && off < tail ){
				
				if( block_size > 0 ){
					block_size--;
				}else{
					if( block_size == 0 ){
						//ブロックサイズの読み込み.
						block_size = ( ((0xFF)&cutter.getBits(8)) << 8 );
						block_size |= (0xFF)&cutter.getBits(8);
						//このブロック用のハフマンテーブルの読み込み.
						loadStaticHuffmanTable();
						block_size--;
					}
				}

				c = hufC.decode(cutter);
				if( (c & 0x100) == 0 ){
					b[off++] = dic[now_pos++] = (byte)c;	//辞書に登録
					now_pos &= dic_mask;		//posを辞書内に制限
					count++;
				}
				else{
					//一致長を取り出す
	  				match_len = (c & 0x0FF) + THRESHOLD;
					//一致場所を取り出す
					match_pos = (now_pos - hufP.decode(cutter) - 1) & dic_mask;
					count++;
				}
			}

			if( match_len > 0 ){
				int copy_len = Math.min( match_len, tail - off );
				int lest;
				count += copy_len;
                match_len -= copy_len;
				if( copy_len <= (lest = dic.length - match_pos) ){
					while( copy_len > 0 ){
						dic[now_pos++] = b[off++] = dic[match_pos++];
						now_pos &= dic_mask;
						copy_len--;
					}
				}
				else{
				    copy_len -= lest;
					while( lest > 0 ){
						dic[now_pos++] = b[off++] = dic[match_pos++];
						now_pos &= dic_mask;
						lest--;
					}
					match_pos = 0;
					while( copy_len > 0 ){
						dic[now_pos++] = b[off++] = dic[match_pos++];
						now_pos &= dic_mask;
						copy_len--;
					}
				}
			}
		}while( off < tail );
	}

	protected long getCount(){
		return count;
	}
}
