/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of version 3 of the GNU Lesser General Public License
 * (LGPL-3.0-only) as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received copies of the GNU Lesser General Public License
 * and the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package xaero.pac.common.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Reflection {

	public static Field getFieldReflection(Class<?> c, String obfuscatedName, String shortObfuscatedName, String name) {
		Field field = null;
		try {
			field = c.getDeclaredField(obfuscatedName);
		} catch (NoSuchFieldException e) {
			field = getFieldReflection(c, shortObfuscatedName, name);
		}
		return field;
	}
	
	public static Field getFieldReflection(Class<?> c, String obfuscatedName, String name) {
		Field field = null;
		try {
			field = c.getDeclaredField(obfuscatedName);
		} catch (NoSuchFieldException e) {
			try {
				field = c.getDeclaredField(name);
			} catch (NoSuchFieldException e1) {
				throw new RuntimeException(e1);
			}
		}
		return field;
	}
	
	@SuppressWarnings("unchecked")
	public static <A, B> B getReflectFieldValue(A parentObject, Field field) {
		boolean accessibleBU = field.isAccessible();
		field.setAccessible(true);
		B result = null;
		try {
			result = (B) field.get(parentObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
		field.setAccessible(accessibleBU);
		return result;
	}

	public static <A, B> void setReflectFieldValue(A parentObject, Field field, B value) {
		boolean accessibleBU = field.isAccessible();
		field.setAccessible(true);
		try {
			field.set(parentObject, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		field.setAccessible(accessibleBU);
	}
	
	public static Method getMethodReflection(Class<?> c, String obfuscatedName, String shortObfuscatedName, String name, Class<?>... parameters) {
		Method method = null;
		try {
			method = c.getDeclaredMethod(obfuscatedName, parameters);
		} catch (NoSuchMethodException e) {
			method = getMethodReflection(c, shortObfuscatedName, name, parameters);
		}
		return method;
	}
	
	public static Method getMethodReflection(Class<?> c, String obfuscatedName, String name, Class<?>... parameters) {
		Method method = null;
		try {
			method = c.getDeclaredMethod(obfuscatedName, parameters);
		} catch (NoSuchMethodException e) {
			try {
				method = c.getDeclaredMethod(name, parameters);
			} catch (NoSuchMethodException e1) {
				throw new RuntimeException(e1);
			}
		}
		return method;
	}
	
	@SuppressWarnings("unchecked")
	public static <A, B> B getReflectMethodValue(A parentObject, Method method, Object... arguments) {
		boolean accessibleBU = method.isAccessible();
		method.setAccessible(true);
		B result = null;
		try {
			result = (B) method.invoke(parentObject, arguments);
		} catch (Exception e) {
			e.printStackTrace();
		}
		method.setAccessible(accessibleBU);
		return result;
	}
	
}
