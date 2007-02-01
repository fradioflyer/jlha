/*
 * $RCSfile: DynamicHuffmanC.java,v $ $Date: 2000/04/15 17:28:07 $ 
 * Ver. $Name:  $  Source revision. $Revision: 1.1.1.1 $
 *
 * Copyright 2000 by TURNER.
 */

package jp.gr.java_conf.turner.util.lha;


class DynamicHuffmanC extends DynamicHuffman
{
	
	/**
	 * 動的ハフマンコード復号クラスのコンストラクタ
	 * @param n_max	対象とするコードの数（範囲）
	 */
	DynamicHuffmanC( int 	n_max )
	{
		super( n_max );
		
		init();
	}
	
	private void init()
	{
		int i,j;
		
		for( i = 0, j = nodes.length -1; i < n_max; i++, j--) {
			Leaf leaf_wk = new Leaf();
			nodes[j] = leaf_wk;
			nodes[j].index = j;
			nodes[j].freq = 1;
			leaf_wk.real_code = i;
		}

		i = nodes.length - 1;
		while (j >= 0) {
			Branch branch_wk = new Branch();
			nodes[j] = branch_wk;
			nodes[j].index = j;

			branch_wk.child_0 = nodes[i];
			branch_wk.child_1 = nodes[i-1];

			branch_wk.child_0.parent = branch_wk;
			branch_wk.child_1.parent = branch_wk;

			i -= 2;
			j--;
		}
		
		treeRoot = nodes[0];
	}
}