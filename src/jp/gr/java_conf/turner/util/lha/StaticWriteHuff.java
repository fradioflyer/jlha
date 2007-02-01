/*
 * $RCSfile: StaticWriteHuff.java,v $ $Date: 2001/11/21 17:08:59 $ 
 * Ver. $Name:  $  Source revision. $Revision: 1.8 $
 *
 * Copyright 2000 by TURNER.
 */

package jp.gr.java_conf.turner.util.lha;

import java.io.*;

/**
 * エンコード用静的ハフマン辞書.
 *
 * @auther TURNER
 */
class StaticWriteHuff extends StaticHuffman implements ItfWriteHuff
{
	//protected Leaf[] encodeTable;

	/**
	 * falseの時はハフマンツリー構築のための頻度収集モード.
	 */
	protected boolean encodeModeFlg = false;

	/**
	 * 頻度収集モードから実際にエンコードするモードに移行する関数.
	 * ハフマンツリーオブジェクトは使い捨てなので元に戻す方法はない.
	 *
	 */
	protected void setEncodeMode(){
		encodeModeFlg = true;
	}

	/**
	 * ハフマンコード復号クラスのコンストラクタ.
	 * 
	 * @param table_size ハフマン辞書の大きさ
	 */
	protected StaticWriteHuff( int table_size ){
		super( table_size );
		initFreq();
	}

	/**
	 * ハフマンコードにエンコードする.
	 * 
	 * @param real_code    元データコード
	 * @param packer       ビットパッカー
	 *
	 * @throws IOException IOエラーが起こったとき
	 */
	public void encode( int real_code, BitPacker packer )
		throws IOException
	{
		if( encodeModeFlg ){
			Leaf leaf = leafs[real_code];
			packer.putBits( leaf.code, leaf.code_len );
		}else{
			countFreq( real_code ); /* 頻度を数える */
		}
	}



	/**
	 * ハフマンテーブルをファイルに書き込む.
	 *
	 * @param effective_len_bits 有効符号数の読み込みビット数
	 * @param special_index      空白インデックス指定
	 * @param packer             ビットパッカー（ストリーム）
	 *
	 * @param IOException IOエラーが起こったとき
	 */
	protected void makeTreeAndSaveTo(  int effective_len_bits, int special_index,
			BitPacker packer )
		throws IOException
	{

		int i;
		for( i = leafs.length - 1; i >= 0; i-- ){
			if( leafs[i].freq > 0 )break;
		}
		i++;
		if( i < leafs.length ){
			Leaf[] tmp_leafs = new Leaf[i];
			System.arraycopy( leafs, 0, tmp_leafs, 0, i );
			leafs = tmp_leafs;
		}

		Leaf[] sort = makeProvisionalTree();
		Leaf[] bakup = leafs;
		if( sort.length > 1 ){
			makeCodeLen(sort);
		}else{
			leafs = new Leaf[1];
			leafs[0] = sort[0];
		}

		//コードが１つしかないテーブルの場合
		//ハフマンコードの割り振りはいらない。
		if( sort.length > 1 ){
			makeTableCode();
		}
		
		restoreTree();

		writeTableLen( effective_len_bits, special_index, packer );

		//encodeTable = new Leaf[table_size];
		//for( int i=0; i < leafs.length; i++ ){
		//	encodeTable[leafs[i].real_code] = leafs[i];
		//}

		leafs = bakup;
		setEncodeMode();

	}

	/**
	 * 頻度を集計するために初期化する.
	 * 
	 */
	private void initFreq(){
		leafs = new Leaf[table_size];
		for( int i=0; i<table_size; i++ ){
			leafs[i] = new Leaf();
			leafs[i].real_code = i;
		}
	}

	/**
	 * 頻度を集計する.
	 *
	 * @param code 頻度を数えるコード
	 */
	private void countFreq( int code ){
		leafs[code].freq++;
	}

	/**
	 * 符号長をファイルに書き込む.
	 *
	 * @param effective_len_bits 有効符号数のビット数
	 * @param special_index      空白インデックス指定
	 * @param packer             ビットパッカー（ストリーム）
	 *
	 * @param IOException ＩＯエラーが起こったとき
	 */
	protected void writeTableLen( int effective_len_bits, int special_index, 
			BitPacker packer )
		throws IOException
	{
		if( leafs.length == 1 ){
			packer.putBits( 0, effective_len_bits );
			packer.putBits( leafs[0].real_code, effective_len_bits );
		}
		else{
			packer.putBits( leafs.length, effective_len_bits );
			int c;
			for( int i = 0; i < leafs.length; i++ ){
				if( i == special_index ){
					int j = 0;
					while( leafs[i].code_len == 0 && j < 3 ){
						j++; i++;
						if( i == leafs.length ) break;
					}
					packer.putBits( j, 2 );
					if( i == leafs.length ) break;
				}
				c = leafs[i].code_len;
				if( c < 7 ){
					packer.putBits( c, 3 );
				}else{
					packer.putBits( 7, 3 );
					packer.putCluster( 1, c-7 );
					packer.putBit( 0 );
				}
			}
		}
	}

	/**
	 * heap処理のサブ処理.
	 * 暫定ツリー作成関数で使用する.
	 * 
	 * @param i         ヒープ上の処理対象データのインデックス.
	 * @param heap      ヒープ
	 * @param heapsize  ヒープ上で実際に使用している大きさ
	 */
	private void downHeap( int i, TreeNode[] heap, int heapsize ){
		int j;
		TreeNode k;

		k = heap[i];
		while ((j = 2 * i) < heapsize) {
			if ( j+1 < heapsize && heap[j].freq > heap[j+1].freq ){
				j++;
			}
			if (k.freq <= heap[j].freq){
				break;
			}
			heap[i] = heap[j];
			i = j;
		}
		heap[i] = k;
	}

	/**
	 * 暫定ツリー作成関数.
	 * 頻度集計結果を利用して
	 * heapソートの応用でhaffmanツリーを暫定的に構築する.
	 *
	 * @return 頻度freqの昇順にソートされたツリーの葉の配列
	 */
	protected Leaf[] makeProvisionalTree() {
		int i;
		int heap_size;
		TreeNode[] heap = new TreeNode[leafs.length*2];

		heap_size = 1; //ヒープでは０のインデックスは使わないので１からはじめる
		for( i = 0; i < leafs.length; i++ ){
			if( leafs[i].freq > 0 ){
				heap[heap_size] = leafs[i];
				heap_size++;
			}
		}

		//項目がない。ヒープは１から始まっているので
		if( heap_size == 1 ){
			throw new InternalError( "no freq data." );
		}

		Leaf[] code_len_sort = new Leaf[heap_size-1];

		if( heap_size == 2 ){
			treeRoot = heap[1];
			code_len_sort[0] = (Leaf)heap[1];
		}else{

			//* 小さな頻度のノードが頂点にくるようにヒープを正規化する**
			for( i = heap_size / 2; i >= 1; i-- ){
				downHeap( i, heap, heap_size );
			}

			//* 小さな頻度のノードを２つづつ枝で結合しながらツリーを構成してゆく **
			TreeNode j,k;
			Branch l;
			int code_len_sort_i = 0;
			do{
				j = heap[1];
				if( j instanceof Leaf ){
					code_len_sort[code_len_sort_i++] = (Leaf)j;
				}
				heap_size--;
				heap[1] = heap[heap_size];
				heap[heap_size] = null;
				downHeap( 1, heap, heap_size );

				k = heap[1];
				if( k instanceof Leaf ){
					code_len_sort[code_len_sort_i++] = (Leaf)k;
				}
				
				l = new Branch();
				l.freq = j.freq + k.freq;
				l.child_0 = j;
				l.child_1 = k;

				heap[1] = l;
				downHeap( 1, heap, heap_size );
			}while( heap_size > 2 ); //** heap_size = 2のときは０は使わないので残り一つ
			
			treeRoot = l;
		}

		return code_len_sort;

	}

	/**
	 * ツリーの各末端のルートからの距離を測って集計を取る関数.
	 * ルートからの距離を測る際に再帰を使う.
	 *
	 * @param node      部分ツリーのルートノード
	 * @param len_count ルートからの距離の集計値を返す出力引数
	 * @param depth     ひとつ親のノードのルートからの距離
	 * @param code      ハフマンコード
	 */
	private void countLen( TreeNode node, int[] len_count, int depth, int code ){
		if( node instanceof Leaf ){
			len_count[(depth < len_count.length)?depth:len_count.length-1]++;
			((Leaf)node).code = code;
			((Leaf)node).code_len = depth;
		}else{
			countLen( ((Branch)node).child_0, len_count, depth + 1, code << 1  );
			countLen( ((Branch)node).child_1, len_count, depth + 1,(code << 1)+1 );
		}
	}

	/**
	 * ハフマンコードの長さを仮ツリーから作る.
	 *
	 * @auther TURNER
	 */
	protected void makeCodeLen( Leaf[] sort )
	{
		int[] len_count = new int[CODELEN_MAX+1];
		countLen( treeRoot, len_count, 0, 0 );

		int i;
		int cum = 0;
		for( i = CODELEN_MAX; i > 0; i-- ){
			cum += len_count[i] << (CODELEN_MAX - i);
		}
		cum &= 0xFFFF;

		//*** 長すぎるハフマンコードを短くする
		if (cum > 0) {
			System.err.println("17");
			len_count[CODELEN_MAX] -= cum;	/* always len_cnt[16] > cum */
			do {
				for (i = CODELEN_MAX - 1; i > 0; i--) {
					if (len_count[i] > 0) {
						len_count[i]--;
						len_count[i + 1] += 2;
						break;
					}
				}
				cum--;
			} while (cum > 0);
		}

		//** ハフマンコードの符号長を設定する **
		int index = 0;
		for (i = CODELEN_MAX; i > 0; i--) {
			for( int k=0; k < len_count[i]; k++ ) {
				sort[index++].code_len = i;
			}
		}
	}

	public int decode( BitCutter cutter ){
		throw new InternalError( "unsupported method." );
	}

	protected void loadFrom( int a, int b, BitCutter c ){
		throw new InternalError( "unsupported method." );
	}

	protected void readTableLen( int a, int b, BitPacker c ){
		throw new InternalError( "unsupported method." );
	}

}
