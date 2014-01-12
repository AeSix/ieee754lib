/*
 * Copyright (C) 2014  Glenn Lane
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package org.oldhen.ieee754lib;

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