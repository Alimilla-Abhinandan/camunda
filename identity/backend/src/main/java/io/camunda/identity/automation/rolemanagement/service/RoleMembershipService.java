/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.identity.automation.rolemanagement.service;

import io.camunda.identity.automation.rolemanagement.repository.RoleRepository;
import io.camunda.identity.automation.security.CamundaUserDetails;
import io.camunda.identity.automation.security.CamundaUserDetailsManager;
import io.camunda.identity.automation.usermanagement.CamundaGroup;
import io.camunda.identity.automation.usermanagement.service.GroupService;
import io.camunda.identity.automation.usermanagement.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoleMembershipService {

  private final UserService userService;
  private final GroupService groupService;
  private final CamundaUserDetailsManager camundaUserDetailsManager;
  private final RoleRepository roleRepository;

  public RoleMembershipService(
      final UserService userService,
      final GroupService groupService,
      final CamundaUserDetailsManager camundaUserDetailsManager,
      final RoleRepository roleRepository) {
    this.userService = userService;
    this.groupService = groupService;
    this.camundaUserDetailsManager = camundaUserDetailsManager;
    this.roleRepository = roleRepository;
  }

  public List<String> getRolesByUserId(final long userId) {
    final CamundaUserDetails camundaUserDetails = retrieveCamundaUserDetails(userId);
    return camundaUserDetails.getRoles();
  }

  public void assignRoleToUser(final String roleName, final long userId) {
    if (!roleRepository.existsById(roleName)) {
      throw new RuntimeException("role.notFound");
    }

    final CamundaUserDetails camundaUserDetails = retrieveCamundaUserDetails(userId);

    final List<String> roles =
        Stream.concat(camundaUserDetails.getRoles().stream(), Stream.of(roleName)).toList();

    saveUserDetailsWithRoles(camundaUserDetails, roles);
  }

  public void unassignRoleFromUser(final String roleName, final long userId) {
    if (!roleRepository.existsById(roleName)) {
      throw new RuntimeException("role.notFound");
    }

    final CamundaUserDetails camundaUserDetails = retrieveCamundaUserDetails(userId);

    final List<String> roles = new ArrayList<>();
    roles.addAll(
        camundaUserDetails.getRoles().stream().filter(rn -> !rn.equals(roleName)).toList());

    saveUserDetailsWithRoles(camundaUserDetails, roles);
  }

  public List<String> getRolesByGroupId(final Long groupId) {
    final CamundaGroup camundaGroup = groupService.findGroupById(groupId);

    return camundaUserDetailsManager.findGroupAuthorities(camundaGroup.name()).stream()
        .map(GrantedAuthority::getAuthority)
        .map(authorityName -> authorityName.replace("ROLE_", ""))
        .toList();
  }

  public void assignRoleToGroup(final String roleName, final long groupId) {
    if (!roleRepository.existsById(roleName)) {
      throw new RuntimeException("role.notFound");
    }
    final CamundaGroup camundaGroup = groupService.findGroupById(groupId);

    camundaUserDetailsManager.addGroupAuthority(
        camundaGroup.name(), new SimpleGrantedAuthority("ROLE_" + roleName));
  }

  public void unassignRoleFromGroup(final String roleName, final long groupId) {
    if (!roleRepository.existsById(roleName)) {
      throw new RuntimeException("role.notFound");
    }
    final CamundaGroup camundaGroup = groupService.findGroupById(groupId);

    camundaUserDetailsManager.removeGroupAuthority(
        camundaGroup.name(), new SimpleGrantedAuthority("ROLE_" + roleName));
  }

  private void saveUserDetailsWithRoles(
      final CamundaUserDetails camundaUserDetails, final List<String> roles) {
    final UserDetails userDetails =
        User.withUsername(camundaUserDetails.getUsername())
            .password(camundaUserDetails.getPassword())
            .passwordEncoder(Function.identity())
            .roles(roles.toArray(new String[0]))
            .disabled(!camundaUserDetails.isEnabled())
            .build();

    camundaUserDetailsManager.updateUser(userDetails);
  }

  private CamundaUserDetails retrieveCamundaUserDetails(final long userId) {
    final String username = userService.findUserById(userId).getUsername();
    return camundaUserDetailsManager.loadUserByUsername(username);
  }
}
