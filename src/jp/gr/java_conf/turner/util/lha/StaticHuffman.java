/*
 * $RCSfile: StaticHuffman.java,v $ $Date: 2001/11/16 17:28:01 $ 
 * Ver. $Name:  $  Source revision. $Revision: 1.8 $
 *
 * Copyright 2000 by TURNER.
 */

package jp.gr.java_conf.turner.util.lha;

import java.io.*;

/**
 * 静的ハフマン辞書の基本クラス.
 *
 * @auther TURNER
 */
class StaticHuffman  implements ItfHuffman
{

	protected static int CODELEN_MAX = 16;
	protected TreeNode treeRoot = null;
	protected Leaf[] leafs;
	private int shortcuts_bits;
	protected TreeNode[] shortcuts;
	protected int table_size;

	
	/**
	 * ハフマンコード復号クラスのコンストラクタ.
	 * 
	 * @param table_size ハフマン辞書の大きさ
	 */
	protected StaticHuffman( int table_size ){
		this.table_size = table_size;
		//leafs = new Leaf[table_size];
		
		//ショートカットテーブルの大きさを決める
		shortcuts_bits = 4;
		while( (1<<shortcuts_bits) < table_size ){
			shortcuts_bits++;
		}
		shortcuts = new TreeNode[1<<shortcuts_bits];
	}

	
	/**
	 * ツリーをたどってデコードする.
	 *
	 * @param cutter ビットカッター（ストリーム）
	 */
	public int decode( BitCutter cutter )
		throws IOException
	{
		int copy_code = cutter.copyBits( shortcuts_bits );
		TreeNode currentNode = shortcuts[copy_code];
		
		if( currentNode instanceof Leaf ){
			cutter.skipBits( ((Leaf)currentNode).code_len );
		}
		else{
			cutter.skipBits( shortcuts_bits );
			do{
				if( cutter.getBit() == 1 ){
					currentNode = ((Branch)currentNode).child_1;
				}
				else{
					currentNode = ((Branch)currentNode).child_0;
				}
			}while( !(currentNode instanceof Leaf) );
		}

		return ((Leaf)currentNode).real_code;	 
	}
	
	/**
	 * 符号長をファイルから読み込む.
	 *
	 * @param effective_len_bits 有効符号数の読み込みビット数
	 * @param special_index      空白インデックス指定
	 * @param cutter             ビットカッター（ストリーム）
	 */
	protected void readTableLen( int effective_len_bits, int special_index, BitCutter cutter )
		throws IOException
	{
		int i, c;
		
		int effective_len = cutter.getBits( effective_len_bits );
		

		if( effective_len == 0 ){
			leafs = new Leaf[1];
			leafs[0] = new Leaf();

			c = cutter.getBits( effective_len_bits );
			leafs[0].code_len = 0;
			leafs[0].code = 0;
			leafs[0].real_code = c;
		}
		else{
			leafs = new Leaf[effective_len];
			for( i=0; i < leafs.length; i++ ){
				leafs[i] = new Leaf();
			}

			i = 0;
			while( i < effective_len ){
				c = cutter.getBits(3);
				if( c == 7 ){
					c += cutter.getClusterLen( 1 );
				}
				leafs[i].code_len = c;
				i++;
				if( i == special_index ){
					c = cutter.getBits(2);
					while( --c >= 0 ){
						leafs[i++].code_len = 0;
					}
				}
			}
		}
	}
	

	/**
	 *	符号長から符号語をつくる.
	 */
	protected void makeTableCode()
		throws IOException
	{
		int i,j;
		/*
		 *	符号長の出現個数をカウント
		 */
		int[] len_count = new int[CODELEN_MAX + 1];
		for( i=0; i < leafs.length; i++ ){
			if( leafs[i].code_len > CODELEN_MAX ){
				throw new LhaException("Invalid Huffman table.");
			}
			len_count[leafs[i].code_len]++;
		}

		/*
		 *	符号長からハフマン符号を生成
		 *	ハフマンコード長が０ってことはそのコードは割り当てられてないってこと
		 *	だからi=1から処理を開始する。
		 */
		int[] codeStart = new int[len_count.length+1];
		for( i=1; i < (codeStart.length - 1); i++ ){
			codeStart[i+1] = ( codeStart[i] + len_count[i] ) << 1;
		}

		for( i = 1; i < codeStart.length ; i++ ){
			for( j = 0 ; j < leafs.length ; j++ ){
				if( leafs[j].code_len == i ){
					leafs[j].code = codeStart[ i ]++;
					leafs[j].real_code = j; //本来のコード
				}
			}
		}
	}

	/**
	 * 符号語からツリー構造を構築する.
	 */
	protected void restoreTree()
	{
		int i;

		//符号語が１種類しかなかった時の特殊処理
		if( leafs.length == 1 ){
			treeRoot = leafs[0];
			for( i = 0; i < shortcuts.length; i++ ){
				shortcuts[i] = treeRoot;
			}
			return;
		}

		Branch currentNode;
		int mask;

		treeRoot = new Branch(); 
	
		for( i=0; i < leafs.length; i++ ){
			
			//それぞれのハフマンコードを１ビットずつたどりながらツリーを組み立ててゆく
			//ツリーは常にルートからたどり始める
			currentNode = (Branch)treeRoot; 		
			mask = 1 << (leafs[i].code_len - 1);
			for( int j=0; j < leafs[i].code_len; j++ ){

				if( (leafs[i].code & mask) != 0 ){
					//コードのビットが１だったときの処理。
					
					//ハフマンコードの最後にたどり着いたらツリーの葉として登録する。
					if( j == (leafs[i].code_len - 1) ){
						currentNode.child_1 = leafs[i];
					}
					else{
						//もし既にノードが有ればそこをたどってゆく。
						//なければ、新しいノードを作る。
						if( currentNode.child_1 == null ){
							currentNode.child_1 = new Branch();
							
							//ショートカットの配列にセット
							if( j == (shortcuts_bits-1) ){
							    int index = leafs[i].code >>> (leafs[i].code_len - shortcuts_bits);
								shortcuts[index] = currentNode.child_1;
							}
						}
						currentNode = (Branch)currentNode.child_1;
					}
				}
				else{
					//コードのビットが０だったときの処理
					
					//ハフマンコードの最後にたどり着いたらツリーの葉として登録する。
					if( j == (leafs[i].code_len - 1) ){
						currentNode.child_0 = leafs[i];
					}
					else{
						//もし既にノードが有ればそこをたどってゆく。
						//なければ、新しいノードを作る。
						if( currentNode.child_0 == null ){
							currentNode.child_0 = new Branch();
							
							//ショートカットの配列にセット
							if( j == (shortcuts_bits-1) ){
							    int index = leafs[i].code >>> (leafs[i].code_len - shortcuts_bits);
								shortcuts[index] = currentNode.child_0;
							}
						}
						currentNode = (Branch)currentNode.child_0;
					}
				}

				//ビットマスクを１つ進める
				mask >>>= 1;
			}

			//ショートカットの配列にセット
			if( leafs[i].code_len > 0 && leafs[i].code_len <= shortcuts_bits ){
				int index_mask = ((1 << leafs[i].code_len)-1) << (shortcuts_bits - leafs[i].code_len);
				int index = leafs[i].code << (shortcuts_bits - leafs[i].code_len);
				for( int k = index; (k & index_mask) == index; k++ ){
					shortcuts[k] = leafs[i];
				}
			}
		}
	}

	/**
	 * ハフマンテーブルをファイルから読み込む.
	 *
	 * @param effective_len_bits 有効符号数の読み込みビット数
	 * @param special_index      空白インデックス指定
	 * @param cutter             ビットカッター（ストリーム）
	 */
	protected void loadFrom(  int effective_len_bits, int special_index,
			BitCutter cutter )
		throws IOException
	{
		readTableLen( effective_len_bits, special_index, cutter );
		//コードが１つしかないテーブルの場合
		//ハフマンコードの割り振りはいらない。
		if( leafs.length > 1 ){
			makeTableCode();
		}
		restoreTree();
	}

}
