package service.accountservice.entity;

import jakarta.persistence.EntityListeners;
import service.accountservice.listener.AccountEventListener;
import service.shared.models.Account;

@EntityListeners(AccountEventListener.class)
public class AccountEntity extends Account {

}
