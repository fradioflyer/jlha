/*
 * $RCSfile: BitPacker.java,v $ $Date: 2001/11/15 16:38:23 $ 
 * Ver. $Name:  $  Source revision. $Revision: 1.3 $
 *
 * Copyright 2000 by TURNER.
 */
package jp.gr.java_conf.turner.util.lha;

import java.io.*;

/**
 * 任意のビット数のデータを書き出せる出力ストリーム.
 *
 * @author TURNER
 */
class BitPacker extends OutputStream{

	protected OutputStream os;

	protected static final int BYTE_BITS = 8;
	protected int bitbuf = 0;
	protected int bitbuf_count = 0;
	protected long write_counter = 0L;

	protected static final int PACK_MAX = 32 - (BYTE_BITS) + 1;

	protected BitPacker( OutputStream arg_os ){
		os = arg_os;
	}

	protected BitPacker( RandomAccessFile raf ) throws IOException{
		os = new FileOutputStream( raf.getFD() );
	}

	/**
	 *  １バイトに満たない半端なビットを０で埋めた後、１バイト書き込む.
	 * 
	 * @param b    書き込むデータ
	 * 
	 * @author  TURNER
	 */
	public void write( int b )
		throws IOException
	{
		adjustByteAlignment();
		os.write( b );
		write_counter ++;
	}


	/**
	 *  １バイトに満たない半端なビットを０で埋めた後、複数バイト書き込む.
	 * 
	 * @param b     書き込むデータのバイト配列
	 * @param off   書き込むデータの開始オフセット
	 * @param len   書き込むデータの長さ
	 * 
	 * @author      TURNER
	 */
	public void write( byte[] b, int off, int len )
		throws IOException
	{
		adjustByteAlignment();
		os.write( b, off, len );
		write_counter += len;
	}

	/**
	 *  １バイトに満たない半端なビットを０で埋めた後、複数バイト書き込む.
	 * 
	 * @param b     書き込むデータのバイト配列
	 * 
	 * @author      TURNER
	 */
	public void write( byte[] b )
		throws IOException
	{
		this.write( b, 0, b.length );
	}

	/**
	 *  任意のビット数データを書き込む.
	 * 
	 * @param b 書き込むデータ
	 * @param n 書き込むビット数
	 * 
	 * @author TURNER
	 */
	 public void putBits( int b, int n )
	 	throws IOException
	 {
		if( n < 0 || n > PACK_MAX ){
			throw new 
				IllegalArgumentException("arg="+n+" limit:0<=arg<="+PACK_MAX);
		}

		int mask = (( 0xFFFFFFFF >>> bitbuf_count ));
		bitbuf = (bitbuf & ~mask) | ((b << (32-(bitbuf_count+n))) & mask);
		bitbuf_count += n;

		while( bitbuf_count >= BYTE_BITS ){
			writeByte();
		}
	 }

	/**
	 *  1ビット数データを書き込む.
	 * 
	 * @param b 書き込むビット
	 * 
	 * @author TURNER
	 */
	 public void putBit( int b )
	 	throws IOException
	 {	
		if( (b & ~(1)) != 0 ){
			throw new IllegalArgumentException( "bit=0or1" );
		}

		int mask = (( 0xFFFFFFFF >>> bitbuf_count ));
		bitbuf = (bitbuf & ~mask) | (b << (32-(bitbuf_count+1)));
		bitbuf_count ++;

		if( bitbuf_count >= BYTE_BITS ){
			writeByte();
		}
	}

	/**
	 *  同じビットの固まりを書き込む.
	 * 
	 * @param b     書き込むビット
	 * @param len   書き込む長さ
	 * 
	 * @author TURNER
	 */
	public void putCluster( int b, int len )
		throws IOException
	{
		if( len < 0 || len > PACK_MAX ){
			throw new 
				IllegalArgumentException("len="+len+" limit:0<=len<="+PACK_MAX);
		}
		if( (b & ~(1)) != 0 ){
			throw new IllegalArgumentException( "bit=0or1" );
		}

		int mask = (( 0xFFFFFFFF >>> bitbuf_count ));
		if( b == 1 ){
			bitbuf |= mask;
		}else{
			bitbuf &= (~mask);
		}

		bitbuf_count += len;

		while( bitbuf_count >= BYTE_BITS ){
			writeByte();
		}
	}


	/**
	 *  ストリームをクローズする.
	 * 
	 * @author TURNER
	 */
	public void close()
		throws IOException
	{
		this.flush();
		os.close();
	}

	/**
	 *  半端なビットを書き込んだ後ストリームをフラッシュする.
	 * 
	 * @author TURNER
	 */
	public void flush()
		throws IOException
	{
		adjustByteAlignment();
		os.flush();
	}


	/**
	 * １バイト書き込み.
	 * 
	 * @author TURNER
	 */
	private void writeByte()
		throws IOException
	{
		os.write( bitbuf>>>(BYTE_BITS*3) );
		bitbuf <<= BYTE_BITS;
		bitbuf_count -= BYTE_BITS;
		write_counter++;
	}

	/**
	 *  バッファに残っている１バイトに満たない半端なビットのうしろに０を埋めて書き込む.
	 * 
	 * @author TURNER
	 */
	protected void adjustByteAlignment()
		throws IOException
	{
		if( bitbuf_count > 0 ){
			int mask = ( 0xFFFFFFFF >>> bitbuf_count ) ;
			bitbuf &= (~mask);
			os.write( bitbuf >>> (BYTE_BITS*3) );
			bitbuf_count = 0;
			write_counter++;
		}
	}

	/**
	 *  書き込みバイト数カウンタを取得する.
	 * 
	 * @return 書き込みバイト数
	 * 
	 * @author TURNER
	 */
	public long getWriteCounter() {
		return write_counter;
	}

	/**
	 *  書き込みバイト数カウンタをリセットする.
	 *j
	 * @author TURNER
	 */
	public void resetWriteCounter() {
		write_counter = 0L;
	}

}
