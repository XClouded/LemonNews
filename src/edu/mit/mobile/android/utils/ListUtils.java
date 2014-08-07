package edu.mit.mobile.android.utils;
/*
 * Copyright (C) 2010 MIT Mobile Experience Lab
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
import java.util.Collection;

import android.text.TextUtils;

@Deprecated
public class ListUtils {

	/**
	 * Join.
	 *
	 * <p>
	 * Android actually provides a standard join function. This now wraps it, so you should be using
	 * that instead.
	 * </p>
	 *
	 *
	 * @param list
	 * @param delim
	 * @return
	 * @see {@link TextUtils#join(CharSequence,Iterable)}
	 * @deprecated
	 */
	@Deprecated
	public static String join(Collection<String> list, String delim) {

		return TextUtils.join(delim, list);
	}

	/**
	 * Join.
	 *
	 * <p>
	 * Android actually provides a standard join function. This now wraps it, so you should be using
	 * that instead.
	 * </p>
	 *
	 *
	 * @param list
	 * @param delim
	 * @return
	 * @see {@link TextUtils#join(CharSequence,Iterable)}
	 * @deprecated
	 */
	@Deprecated
	public static String joinAsStrings(Collection<? extends Object> list, String delim) {

		return TextUtils.join(delim, list);
	}

}
