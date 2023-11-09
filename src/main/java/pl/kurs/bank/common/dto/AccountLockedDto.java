package pl.kurs.bank.common.dto;

public record AccountLockedDto(long accountId, String message) {
}
