package sk.seges.acris.security.server.spring.user_management.domain.jpa;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.security.GrantedAuthority;

import sk.seges.acris.security.server.spring.user_management.domain.SpringGenericUser;
import sk.seges.acris.security.shared.user_management.domain.api.UserPreferences;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "generic_users")
public class JpaSpringGenericUser extends SpringGenericUser {

	private static final long serialVersionUID = -9107995296795140329L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return super.getId();
	}

	@Column
	public String getDescription() {
		return super.getDescription();
	}

	@Column
	public boolean isEnabled() {
		return super.isEnabled();
	}

	@Column
	public String getUsername() {
		return super.getUsername();
	}

	@Column
	public String getPassword() {
		return super.getPassword();
	}

	@Transient
	public List<String> getUserAuthorities() {
		return super.getUserAuthorities();
	}

	@Transient
	public GrantedAuthority[] getAuthorities() {
		return super.getAuthorities();
	}
	
	@OneToOne(cascade = CascadeType.ALL)
	public UserPreferences getUserPreferences() {
		return super.getUserPreferences();
	}
}