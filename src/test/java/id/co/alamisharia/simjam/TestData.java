package id.co.alamisharia.simjam;

import id.co.alamisharia.simjam.domain.Account;
import id.co.alamisharia.simjam.domain.Group;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface TestData {
    Group desa = Group.builder().name("desa").balance(0D).build();
    Account joko = SimjamApplicationTests.buildAccount(3L, "Joko Widodo,",
            LocalDate.of(1992, 3, 10), "Dusun Pisang Rt 10 Rw 20", desa);
    Account teguh = SimjamApplicationTests.buildAccount(2L, "Teguh Sudibyantoro",
            LocalDate.of(1991, 2, 10), "Jalan Pemekaran No 99", desa);
    Account wawan = SimjamApplicationTests.buildAccount(1L, "Wawan Setiawan",
            LocalDate.of(1990, 1, 10), "Kompleks Asia Serasi No 100", desa);
    List<Account> accounts = Arrays.asList(wawan, teguh, joko);
    Map<String, Account> accountMap = SimjamApplicationTests.buildAccountMap();
}
