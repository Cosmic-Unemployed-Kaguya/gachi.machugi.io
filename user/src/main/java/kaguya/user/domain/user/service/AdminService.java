package kaguya.user.domain.user.service;

import kaguya.user.domain.user.mapper.AdminMapper;
import kaguya.user.domain.user.model.dto.request.BlockReq;
import kaguya.user.domain.user.model.dto.response.GetBlocksInfoRes;
import kaguya.user.domain.user.model.dto.response.GetUserDetailsRes;
import kaguya.user.domain.user.model.dto.response.GetUsersInfoRes;
import kaguya.user.domain.user.model.entity.UserEntity;
import kaguya.user.domain.user.model.entity.UserManagementEntity;
import kaguya.user.domain.user.model.enums.ManagementType;
import kaguya.user.domain.user.model.enums.Role;
import kaguya.user.domain.user.repository.UserManagementRepository;
import kaguya.user.domain.user.repository.UserRepository;
import kaguya.user.global.exception.BusinessException;
import kaguya.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final UserManagementRepository userManagementRepository;

    private final AdminMapper adminMapper;

    @Transactional(readOnly = true)
    public GetUsersInfoRes getUserList() {

        List<UserEntity> userList = userRepository.findAll();

        return adminMapper.userListToGetUsersInfoReq(userList);
    }

    @Transactional(readOnly = true)
    public GetUserDetailsRes getUserDetails(Long idx) {

        UserEntity userEntity = userRepository.findById(idx)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return adminMapper.userEntityToGetUserDetailsReq(userEntity);
    }

    @Transactional
    public void updateNickname(Long idx, String nickname) {

        UserEntity userEntity = userRepository.findById(idx)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 기존 닉네임과 완전히 동일한 경우 400 에러 처리
        if (userEntity.getNickname().equals(nickname)) {
            throw new BusinessException(ErrorCode.SAME_AS_OLD_NICKNAME);
        }

        // 닉네임 중복인지 확인
        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.EXISTS_NICKNAME);
        }

        userEntity.changeNickname(nickname);

        // 어드민이 닉네임을 바꿨다는것에 추가 처리가 있다면 내용 추가 (ex. 바꿨다는 로그 등)
    }

    @Transactional
    public void updateRole(Long idx, Role role) {

        UserEntity userEntity = userRepository.findById(idx)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        userEntity.changeRole(role);
    }

    @Transactional
    public void blockUser(Long userIdx, Long adminIdx, BlockReq blockData) {

        // 임시차단에 종료 날자가 없을 경우
        if (blockData.managementType() == ManagementType.TEMP_BAN && blockData.endDate() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        UserEntity userEntity = userRepository.findById(userIdx)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UserEntity adminEntity = userRepository.findById(adminIdx)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UserManagementEntity managementEntity = adminMapper.blockReqToUserManagementEntity(userEntity, adminEntity, blockData);
        userManagementRepository.save(managementEntity);

        userEntity.changeStatus(blockData.managementType().getMappedStatus());
    }

    @Transactional(readOnly = true)
    public GetBlocksInfoRes getBlockList() {

        List<UserManagementEntity> blockList = userManagementRepository.findAll();

        return adminMapper.userManagementListToGetBlocksInfoRes(blockList);
    }
}