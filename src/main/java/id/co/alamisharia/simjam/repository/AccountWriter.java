package id.co.alamisharia.simjam.repository;

import id.co.alamisharia.simjam.domain.Account;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.r2dbc.core.Parameter;

@WritingConverter
public class AccountWriter implements Converter<Account, OutboundRow> {
    @Override
    public OutboundRow convert(Account source) {
        OutboundRow row = new OutboundRow();
        row.put("social_number", Parameter.from(source.getSocialNumber()));
        row.put("name", Parameter.from(source.getName()));
        row.put("date_of_birth", Parameter.from(source.getDateOfBirth()));
        row.put("address", Parameter.from(source.getAddress()));
        // TODO: 26/11/21 flush the group mapping
        return row;
    }
}
