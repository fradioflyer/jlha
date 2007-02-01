/*
 * $RCSfile: DynamicHuffman.java,v $ $Date: 2000/04/21 15:07:27 $ 
 * Ver. $Name:  $  Source revision. $Revision: 1.2 $
 *
 * Copyright 2000 by TURNER.
 */

package jp.gr.java_conf.turner.util.lha;

import java.io.*;

abstract class DynamicHuffman implements ItfHuffman
{
	protected TreeNode treeRoot = null;
	protected TreeNode[] nodes;
	protected int n_max;
	
	/**
	 * 動的ハフマンコード復号クラスのコンストラクタ
	 * @n_max	対象とするコードの数（範囲）
	 */
	DynamicHuffman( int n_max )
	{
		this.n_max = n_max;
		//Leafと中間ノードの両方をあわせた数の配列
		nodes = new TreeNode[ (n_max * 2) - 1];
	}
	
	/**
	 * ハフマン辞書をデコードし、ツリーを更新する。
	 */
	public int decode( BitCutter cutter )
	    throws IOException
	{
	    TreeNode currentNode = treeRoot;

	    while( !(currentNode instanceof Leaf) ){
	        if( cutter.getBit() == 1 ){
	            currentNode = ((Branch)currentNode).child_1;
	        }
	        else{
	            currentNode = ((Branch)currentNode).child_0;
	        }
	        if( currentNode == null ) throw new NullPointerException();
	    }
	    
		update( (Leaf)currentNode );
        return ((Leaf)currentNode).real_code;    
	}

	/**
	 * デバッグ用
	 */
	public void printArray()
	{
		for( int i = 0; i < nodes.length; i++ ){
			System.out.println( "index:" + nodes[i].index );
			System.out.println( "freq :" + nodes[i].freq );
			if( nodes[i] instanceof Leaf ){
				System.out.println( "-code:" + ((Leaf)nodes[i]).real_code );
			}
			else{
				Branch b = ((Branch)nodes[i]);
				System.out.println( "-child_0 index:" + b.child_0.index + " freq:" + b.child_0.freq );
				System.out.println( "-child_1 index:" + b.child_1.index + " freq:" + b.child_1.freq );
			}
		}
	}

	/**
	 * あるノードの頻度を１加えて、その配下のサブツリー適当な新しい位置に入れ替える。
	 */
	protected TreeNode swap_inc( TreeNode p )
	{
		int p_index, q_index;
		int p_freq;

		p_freq = p.freq;
		p_index = p.index;
		for( q_index = p_index; q_index > 0; q_index-- ){
			if( nodes[q_index-1].freq > p_freq ){
				break;
			}
		}

		if( q_index < p_index ){
			TreeNode q = nodes[q_index];
			Branch new_p_parent_wk;
			Branch new_q_parent_wk;


			new_q_parent_wk = p.parent;
			new_p_parent_wk = q.parent;
			boolean parent_q_child0 = ( new_q_parent_wk.child_0 == p );
			boolean parent_p_child0 = ( new_p_parent_wk.child_0 == q );

			if( parent_q_child0 ){
			    new_q_parent_wk.child_0 = q;
			}
			else{
			    new_q_parent_wk.child_1 = q;
			}

			if( parent_p_child0 ){
			    new_p_parent_wk.child_0 = p;
			}
			else{
			    new_p_parent_wk.child_1 = p;
			}

			p.parent = new_p_parent_wk;
			q.parent = new_q_parent_wk;

			nodes[p_index] = q;
			q.index = p_index;

			nodes[q_index] = p;
			p.index = q_index;
		}
		p.freq++;
		return p.parent;
	}

	/**
	 * 頻度情報をおおよそ半分にしてツリーを再構築する関数。
	 */
	protected void reconst()
	{
		int i;
		Leaf[] leafs = new Leaf[(nodes.length+1)/2];

		//ツリーの葉だけよりわけて頻度を小さくしておく。
		int leaf_index;
		for( i=0, leaf_index = 0; i < nodes.length; i++ ){
			if( nodes[i] instanceof Leaf ){
				Leaf leaf_wk = (Leaf)nodes[i];
				leaf_wk.freq = (leaf_wk.freq + 1) / 2;
				leafs[leaf_index] = leaf_wk;
				leaf_index++;
			}
		}
		
		
		//葉のデータからツリーを再構築する。
		Branch branch_wk;
		TreeNode child_0, child_1;
		leaf_index = leafs.length -1;
		int node_index = nodes.length - 1;
		int pair_index = nodes.length - 2;

		while( node_index >= 0 ){
			while( node_index >= pair_index ){
				nodes[node_index] = leafs[leaf_index];
				nodes[node_index].index = node_index;
				node_index--; leaf_index--;
			}

			child_0 = nodes[pair_index+1];
			child_1 = nodes[pair_index];
			branch_wk = new Branch();
			branch_wk.freq = child_0.freq + child_1.freq;
			branch_wk.child_0 = child_0;
			branch_wk.child_1 = child_1;
			child_0.parent = branch_wk;
			child_1.parent = branch_wk;

			while( leaf_index >= 0 && leafs[leaf_index].freq <= branch_wk.freq ){
				nodes[node_index] = leafs[leaf_index];
				nodes[node_index].index = node_index;
				node_index--; leaf_index--;
			}

			nodes[node_index] = branch_wk;
			nodes[node_index].index = node_index;
			node_index--;
			pair_index -= 2;
		}

		treeRoot = nodes[0];
	}

	/**
	 * 動的ハフマンツリーをアップデートする.
	 * さらに頻度データの合計（ルートのノードの頻度）が
	 * 0x8000をこえたら頻度情報をおおよそ半分にして
	 * ツリーを再構築する。
	 * @param p 頻度を１増やすツリーの葉
	 */
	protected void update( Leaf p )
	{
		if (treeRoot.freq == 0x8000) {
			reconst();
		}

		TreeNode q = p;
		do {
			q = swap_inc(q);
		} while (q != treeRoot);
		q.freq++;
	}
}
