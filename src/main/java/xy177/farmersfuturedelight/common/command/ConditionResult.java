package xy177.farmersfuturedelight.common.command;

final class ConditionResult {
    final boolean matches;
    final int length;

    ConditionResult(boolean matches, int length) {
        this.matches = matches;
        this.length = length;
    }
}
