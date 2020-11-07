package epistemic;


public class PropositionTest {
//
//    @ParameterizedTest
//    @MethodSource(value = "validTestPropositionFixture")
//    public void getKey(@PropAggregator NormalizedWrappedLiteral currentProposition) {
//        assertTrue(currentProposition.isNormalized(), "key should be normalized");
//    }
//
//    @ParameterizedTest
//    @MethodSource(value = "validTestPropositionFixture")
//    public void getValue(@PropAggregator SingleValueProposition currentProposition) {
//        assertNotNull(currentProposition.getValue(), "value should not be null");
//        //assertTrue(currentProposition.getValue().isNormalized(), "value should be normalized");
////        assertTrue(currentProposition.getValue().isGround(), "value should be ground");
//    }
//
//    @ParameterizedTest
//    @MethodSource(value = "validTestPropositionFixture")
//    public void getKeyLiteral(@PropAggregator SingleValueProposition currentProposition) {
//        assertNotNull(currentProposition.getKeyLiteral(), "key literal should not be null");
//        assertEquals(currentProposition.getKeyLiteral(), currentProposition.getKey().getCleanedLiteral(), "key literal should not be the same as the original wrapped key literal");
//        assertTrue(new WrappedLiteral(currentProposition.getKeyLiteral()).isNormalized(), "key literal should be normalized");
//    }
//
//    @ParameterizedTest
//    @MethodSource(value = "validTestPropositionFixture")
//    public void getValueLiteral(@PropAggregator SingleValueProposition currentProposition) {
//        assertNotNull(currentProposition.getValueLiteral(), "value literal should not be null");
////        assertEquals(currentProposition.getValueLiteral(), currentProposition.getValue().getCleanedLiteral(), "value literal should not be the same as the original wrapped value literal");
//        assertTrue(new WrappedLiteral(currentProposition.getValueLiteral()).isNormalized(), "value literal should be normalized");
//    }
//
//    @ParameterizedTest
//    @MethodSource(value = "validTestPropositionFixture")
//    public void setValue(@PropAggregator SingleValueProposition currentProposition) {
////        currentProposition.setValue(new WrappedLiteral(createLiteral("hand", createString("Bob"))).getNormalizedWrappedLiteral());
//        assertTrue(new WrappedLiteral(currentProposition.getValueLiteral()).isNormalized(), "set value should be rejected since it doesnt unify with the key");
//    }
//
//    @ParameterizedTest
//    @MethodSource(value = "validTestPropositionFixture")
//    public void testEquals(@PropAggregator SingleValueProposition currentProposition) {
//        var clonedProposition = cloneProposition(currentProposition);
//        assertEquals(clonedProposition, currentProposition, "clone should equal current prop");
//    }
//
//    @ParameterizedTest
//    @MethodSource(value = "validTestPropositionFixture")
//    public void testHashCode(@PropAggregator SingleValueProposition currentProposition) {
//        var clonedProposition = cloneProposition(currentProposition);
//        assertEquals(clonedProposition.hashCode(), currentProposition.hashCode(), "clone hash should equal current prop hash");
//    }
//
//    @ParameterizedTest
//    @MethodSource(value = "failToUnifyFixture")
//    @DisplayName("Fail to unify key/value in proposition")
//    public void failToUnifyFixture(@WrappedLiteralArg WrappedLiteral key, @WrappedLiteralArg WrappedLiteral value) {
//        Exception exception = assertThrows(IllegalArgumentException.class, () -> new SingleValueProposition(key, value));
//        assertEquals(exception.getMessage(), "The literalValue can not unify the literalKey. Failed to create Proposition.");
//    }
//
//    @ParameterizedTest
//    @MethodSource(value = "valueNotGroundFixture")
//    @DisplayName("Fail to create proposition, value must be ground.")
//    public void valueNotGroundFixture(@WrappedLiteralArg WrappedLiteral key, @WrappedLiteralArg WrappedLiteral value) {
//        Exception exception = assertThrows(IllegalArgumentException.class, () -> new SingleValueProposition(key, value));
//        assertEquals(exception.getMessage(), "literalValue is not ground");
//
//    }
//    private static Stream<Arguments> validTestPropositionFixture() {
//        return Stream.of(
//                Arguments.of("hand('Alice', Card)", "hand('Alice', 'AA')"),
//                Arguments.of("ns::hand('Alice', Card)", "hand('Alice', 'AA')"),
//                Arguments.of("hand('Alice', Card)", "ns::hand('Alice', 'AA')"),
//                Arguments.of("ns::hand('Alice', Card)", "ns::hand('Alice', 'AA')")
//        );
//    }
//
//    private static Stream<Arguments> failToUnifyFixture() {
//        return Stream.of(
//                Arguments.of("test('Bob')", "test('Bob', 'Test')"),
//                Arguments.of("test('Alice', 'Test')", "test('Bob', 'Test')")
//        );
//    }
//
//    private static Stream<Arguments> valueNotGroundFixture() {
//        return Stream.of(
//                Arguments.of("test('Bob', Test)", "test('Bob', Test)"),
//                Arguments.of("test('Alice', Test)", "test('Bob', _)")
//        );
//    }
//
//    private SingleValueProposition cloneProposition(SingleValueProposition current) {
//        if (current == null)
//            return null;
//
//        return new SingleValueProposition(current.getKey(), current.getSingleValue());
//    }
}