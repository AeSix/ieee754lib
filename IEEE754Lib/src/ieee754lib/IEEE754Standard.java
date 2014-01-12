package ieee754lib;

import java.math.BigInteger;

public enum IEEE754Standard implements IEEE754Format
{
	HALF(5, 10, 15),
	SINGLE(8, 23, 127),
	DOUBLE(11, 52, 1023),
	QUADRUPLE(15, 112, 16383),
	;
	
	private final int exponentLength;
	private final int mantissaLength;
	private final BigInteger exponentBias;
	
	private IEEE754Standard(int exponentLength, int mantissaLength,
			int exponentBias)
	{
		this.exponentLength = exponentLength;
		this.mantissaLength = mantissaLength;
		this.exponentBias = BigInteger.valueOf(exponentBias);
	}

	@Override
	public int getExponentLength()
	{
		return exponentLength;
	}

	@Override
	public int getMantissaLength()
	{
		return mantissaLength;
	}

	@Override
	public BigInteger getExponentBias()
	{
		return exponentBias;
	}
}