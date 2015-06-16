package org.jpl7;

import java.util.Map;

import org.jpl7.fli.Prolog;
import org.jpl7.fli.term_t;

/**
 * This class supports Java representations of Prolog variables.
 * <p>
 * 
 * A jpl.Variable instance is equivalent to a variable in a fragment of Prolog source text: it is *not* a "live" variable within a Prolog stack or heap. A corresponding Prolog variable is created only
 * upon opening a Query whose goal refers to a Variable (and then only temporarily).
 * 
 * <hr>
 * <i> Copyright (C) 2004 Paul Singleton
 * <p>
 * Copyright (C) 1998 Fred Dushin
 * <p>
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Library Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * <p>
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Library Public License for more details.
 * <p>
 * </i>
 * <hr>
 * 
 * @author Fred Dushin <fadushin@syr.edu>
 * @version $Revision$
 */
public class Variable extends Term {
	/**
	 * the integral part of the next automatic variable name to be allocated
	 */
	private static long n = 0;

	protected transient int index; // only used by (redundant?)

	/**
	 * the name of this Variable
	 */
	public final String name;

	/**
	 * defined between Query.open() and Query.get2()
	 */
	protected transient term_t term_ = null;

	/**
	 * Create a new Variable with new sequential name of the form "_261".
	 * 
	 */
	public Variable() {
		this.name = "_" + Long.toString(n++); // e.g. _0, _1 etc.
	}

	/**
	 * Create a new Variable with 'name' (which must not be null or ""), and may one day be constrained to comply with traditional Prolog syntax.
	 * 
	 * @param name
	 *            the source name of this Variable
	 */
	public Variable(String name) {
		if (name == null) {
			throw new JPLException("name cannot be null");
		} else if (name.equals("")) {
			throw new JPLException("name cannot be empty String");
		} else {
			this.name = name;
		}
	}

	/**
	 * The (nonexistent) args of this Variable
	 * 
	 * @throws JPLException
	 * 
	 * @return the (nonexistent) args of this Variable (never)
	 */
	public Term[] args() {
		throw new JPLException("args() is undefined for Variable");
	}

	/**
	 * returns, as an int, the arity of a Term
	 * 
	 * @return the arity of a Term
	 */
	public int arity() {
		throw new JPLException("arity() is undefined for Variable");
	};

	/**
	 * A Variable is equal to another if their names are the same and they are not anonymous.
	 * 
	 * @param obj
	 *            The Object to compare.
	 * @return true if the Object is a Variable and the above condition apply.
	 */
	public final boolean equals(Object obj) {
		return obj instanceof Variable && !this.name.equals("_")
				&& this.name.equals(((Variable) obj).name);
	}

	/**
	 * If this Variable instance is not an anonymous or (in dont-tell-me mode) a dont-tell-me variable, and its binding is not already in the varnames_to_Terms Map, put the result of converting the
	 * term_t to which this variable has been unified to a Term in the Map, keyed on this Variable's name.
	 * 
	 * @param varnames_to_Terms
	 *            A Map of bindings from variable names to JPL Terms.
	 * @param vars_to_Vars
	 *            A Map from Prolog variables to JPL Variables.
	 */
	protected final void getSubst(Map<String, Term> varnames_to_Terms,
			Map<term_t, Variable> vars_to_Vars) {
		// NB a Variable.name cannot be "" i.e. of 0 length
		// if (!(this.name.charAt(0) == '_') && varnames_to_Terms.get(this.name) == null) {
		if (tellThem() && varnames_to_Terms.get(this.name) == null) {
			varnames_to_Terms.put(this.name, Term.getTerm(vars_to_Vars, this.term_));
		}
	}

	public boolean hasFunctor(String name, int arity) {
		throw new JPLException("hasFunctor() is undefined for Variable");
	}

	public boolean hasFunctor(int value, int arity) {
		throw new JPLException("hasFunctor() is undefined for Variable");
	}

	public boolean hasFunctor(double value, int arity) {
		throw new JPLException("hasFunctor() is undefined for Variable");
	}

	/**
	 * the lexical name of this Variable
	 * 
	 * @return the lexical name of this Variable
	 */
	public final String name() {
		return this.name;
	}

	/**
	 * To put a Variable, we must check whether a (non-anonymous) variable with the same name has already been put in the Term. If one has, then the corresponding Prolog variable has been stashed in
	 * the varnames_to_vars Map, keyed by the Variable name, so we can look it up and reuse it (this way, the sharing of variables in the Prolog term reflects the sharing of Variable names in the
	 * Term. Otherwise, if this Variable name has not already been seen in the Term, then we put a new Prolog variable and add it into the Map (keyed by this Variable name).
	 * 
	 * @param varnames_to_vars
	 *            A Map from variable names to Prolog variables.
	 * @param term
	 *            A (previously created) term_t which is to be set to a (new or reused) Prolog variable.
	 */
	protected final void put(Map<String, term_t> varnames_to_vars, term_t term) {
		term_t var;
		// if this var is anonymous or as yet unseen, put a new Prolog variable
		if (this.name.equals("_") || (var = (term_t) varnames_to_vars.get(this.name)) == null) {
			this.term_ = term;
			this.index = varnames_to_vars.size(); // i.e. first var in is #0 etc.
			Prolog.put_variable(term);
			if (!this.name.equals("_")) {
				varnames_to_vars.put(this.name, term);
			}
		} else {
			this.term_ = var;
			Prolog.put_term(term, var);
		}
	}

	/**
	 * whether, according to prevailing policy and this Variable's name, its binding (if any) should be returned in a substitution (i.e. unless it's anonymous or we're in dont-tell-me mode and its a
	 * dont-tell-me variable)
	 * 
	 * @return whether, according to prevailing policy and this Variable's name, its binding (if any) should be returned in a substitution
	 */
	private final boolean tellThem() {
		return !(this.name.equals("_") || org.jpl7.JPL.modeDontTellMe && this.name.charAt(0) == '_');
	}

	/**
	 * Returns a Prolog source text representation of this Variable
	 * 
	 * @return a Prolog source text representation of this Variable
	 */
	public String toString() {
		return this.name;
	}

	/**
	 * returns the type of this subclass of Term, i.e. Prolog.VARIABLE
	 * 
	 * @return the type of this subclass of Term, i.e. Prolog.VARIABLE
	 */
	public final int type() {
		return Prolog.VARIABLE;
	}

	/**
	 * returns the typeName of this subclass of Term, i.e. "Variable"
	 * 
	 * @return the typeName of this subclass of Term, i.e. "Variable"
	 */
	public String typeName() {
		return "Variable";
	}

	// public Object jrefToObject() {
	// throw new JPLException("jpl.Variable#jrefToObject(): term is not a jref");
	// }

	// /**
	// * throws a JPLException (arg(int) is defined only for Compound and Atom)
	// *
	// * @return the ith argument (counting from 1) of this Variable (never)
	// */
	// public final Term arg(int i) {
	// throw new JPLException("jpl.Variable#arg(int) is undefined");
	// }

	// /**
	// * Tests the lexical validity of s as a variable's name
	// *
	// * @return the lexical validity of s as a variable's name
	// * @deprecated
	// */
	// private boolean isValidName(String s) {
	// if (s == null) {
	// throw new java.lang.NullPointerException(); // JPL won't call it this way
	// }
	// int len = s.length();
	// if (len == 0) {
	// throw new JPLException("invalid variable name");
	// }
	// char c = s.charAt(0);
	// if (!(c == '_' || c >= 'A' && c <= 'Z')) {
	// return false;
	// }
	// for (int i = 1; i < len; i++) {
	// c = s.charAt(i);
	// if (!(c == '_' || c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9')) {
	// return false;
	// }
	// }
	// return true;
	// }

	// /**
	// * Returns a debug-friendly String representation of an Atom.
	// *
	// * @return a debug-friendly String representation of an Atom
	// * @deprecated
	// */
	// public String debugString() {
	// return "(Variable " + toString() + ")";
	// }

}