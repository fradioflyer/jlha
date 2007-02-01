/*
 * $RCSfile: CRC16.java,v $ $Date: 2001/11/16 17:54:49 $ 
 * Ver. $Name:  $  Source revision. $Revision: 1.4 $
 *
 * Copyright 2000 by TURNER.
 */

package jp.gr.java_conf.turner.util.lha;

/**
 * CRC16計算用クラス.
 * このクラスではcharをunsigned short の代わりに用いている.
 * 
 * @author		TURNER
 */
public class CRC16{

	static char[] crctable;

	private int crc = 0;
	private static final int CRCPOLY = 0xA001;
	private static final int BYTE_BITS = 8;
	private static final int BYTE_MAX = (1<<BYTE_BITS)-1;

	/**
	 * CRC16オブジェクトを生成します.
	 */
	public CRC16() {
		if( crctable == null ){
			init();
		}
	}
   

	/**
	 * CRC16の値を１バイトの引数で更新します.
	 * @param b CRC16を計算するデータ
	 */
	public void update(byte b) {
		crc = crctable[(crc ^ (b)) & 0xFF] ^ (crc >> BYTE_BITS);
	}

	/**
	 * CRC16の値をバイトの配列で更新します.
	 * 
	 * @param b CRC16を計算するデータの配列
	 * @param off データの開始位置を示す配列上のインデックス
	 * @param len 実際に計算するデータのバイト数
	 */
	public void update(byte[] b, int off, int len) {
		int end = (off+len);
		for( int i = off; i < end; i++ ){
			crc = crctable[(crc ^ (b[i])) & 0xFF] ^ (crc >> BYTE_BITS);
		}
	}

	/**
	 * CRCの値をbyteの配列で更新します.
	 * 
	 * @param b CRC16を計算するbyteの配列
	 */
	public void update(byte[] b) {
		update( b, 0, b.length);
	}

	/**
	 * CRC16の値を０にリセットします.
	 */
	public void reset() {
		crc = 0;
	}

	/**
	 * CRC16の値を取得します.
	 */
	public int getValue() {
		return crc & 0x0000ffff;
	}


	/*
	 * create table.
	 */
	private void init()
	{
		int    i, j, r;
		char[] tmp = new char[BYTE_MAX+1];

		for (i = 0; i <= BYTE_MAX; i++) {
			r = i;
			for (j = 0; j < 8; j++){
				if ((r & 1) != 0){
					r = (r >> 1) ^ CRCPOLY;
				}
				else{
					r >>= 1;
				}
			}
			tmp[i] = (char)r;
		}
		crctable = tmp;
	}

}
