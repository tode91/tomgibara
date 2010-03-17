package com.tomgibara.crinch.lattice;

public interface JoinSemiLattice<E> extends SemiLattice<E> {

	E join(E a, E b);
	
	JoinSemiLattice<E> boundedJoinSemiLattice(E top);

	E getTop();
	
	boolean isBoundedAbove();
	
}