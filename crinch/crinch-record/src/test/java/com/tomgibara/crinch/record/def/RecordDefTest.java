package com.tomgibara.crinch.record.def;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class RecordDefTest extends TestCase {

	private static List<ColumnType> types(ColumnType... types) {
		return Arrays.asList(types);
	}
	
	private static List<ColumnOrder> orders(ColumnOrder... orders) {
		return Arrays.asList(orders);
	}

	public void testTrailing() {
		try {
			RecordDef.fromScratch().type(ColumnType.INT_PRIMITIVE).build();
			fail();
		} catch (IllegalStateException e) {
			/* expected */
		}

		try {
			RecordDef.fromScratch().order(null).build();
			fail();
		} catch (IllegalStateException e) {
			/* expected */
		}
		
		try {
			RecordDef.fromScratch().property("a", "b").build();
			fail();
		} catch (IllegalStateException e) {
			/* expected */
		}
		
		try {
			RecordDef.fromScratch().type(ColumnType.INT_PRIMITIVE).add().build().asBasisToBuild().select(0).build();
			fail();
		} catch (IllegalStateException e) {
			/* expected */
		}
	}
	
	public void testWithOrdering() {
		
		RecordDef def = RecordDef.fromTypes(types(ColumnType.INT_PRIMITIVE, ColumnType.BOOLEAN_PRIMITIVE)).build().withOrdering(orders(new ColumnOrder(0, true, true), new ColumnOrder(1, false, false))).asBasis();
		assertEquals(0, def.getColumns().get(0).getOrder().getPrecedence());
		assertEquals(1, def.getColumns().get(1).getOrder().getPrecedence());
		RecordDef sub = def.withOrdering(orders());
		assertNull(sub.getColumns().get(0).getOrder());
		assertNull(sub.getColumns().get(1).getOrder());
		
		sub = sub.withOrdering(orders(null, new ColumnOrder(0, true, false)));
		assertNull(sub.getColumns().get(0).getOrder());
		assertEquals(0, sub.getColumns().get(1).getOrder().getPrecedence());
	}
	
	public void testColumnProperties() {
		RecordDef def = RecordDef.fromScratch().type(ColumnType.INT_PRIMITIVE).property("key1", "value1").property("key2", "value2").add().build();
		Map<String, String> props;
		props = def.getColumns().get(0).getProperties();
		assertEquals(2, props.size());
		assertEquals("value1", props.get("key1"));
		assertEquals("value2", props.get("key2"));
		RecordDef sub = def.asBasisToBuild().select(0).property("key3", "value3").property("key2", null).add().build();
		props = sub.getColumns().get(0).getProperties();
		assertEquals(2, props.size());
		assertEquals("value1", props.get("key1"));
		assertEquals("value3", props.get("key3"));
	}
	
}