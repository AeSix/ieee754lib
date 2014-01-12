package ieee754lib;

import java.math.BigInteger;

public interface IEEE754Format
{
	int getExponentLength();
	int getMantissaLength();
	BigInteger getExponentBias();
}