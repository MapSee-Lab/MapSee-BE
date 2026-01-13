package kr.suhsaechan.mapsee.auth.dto;

import kr.suhsaechan.mapsee.member.constant.MemberRole;
import kr.suhsaechan.mapsee.member.entity.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Getter
public class CustomUserDetails implements UserDetails, Principal {

  private final Member member;

  public CustomUserDetails(Member member) {
    this.member = member;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    MemberRole role = member.getMemberRole();
    if (role == null) {
      role = MemberRole.ROLE_USER; // 기본값 fallback
    }
    return Collections.singletonList(
        new SimpleGrantedAuthority(role.name()));
  }

  @Override
  public String getPassword() {
    return ""; // 소셜 로그인 회원은 패스워드 미사용
  }

  @Override
  public String getUsername() {
    return member.getEmail(); // email을 username으로 사용
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return !member.isDeleted();
  }

  public UUID getMemberId() {
    return member.getId();
  }

  @Override
  public String getName() {
    return member.getName();
  }
}
