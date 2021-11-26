package id.co.alamisharia.simjam.repository;

import id.co.alamisharia.simjam.domain.Account;
import io.r2dbc.spi.Row;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.time.LocalDate;

@ReadingConverter
public class AccountReader implements Converter<Row, Account> {
    @Override
    public Account convert(Row source) {
        return Account.builder()
                .socialNumber(source.get("social_number", Long.class))
                .name(source.get("name", String.class))
                .dateOfBirth(source.get("date_of_birth", LocalDate.class))
                // TODO: 26/11/21 read the group mapping
                .build();
    }
}
