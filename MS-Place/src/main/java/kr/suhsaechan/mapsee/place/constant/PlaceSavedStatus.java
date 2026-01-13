package kr.suhsaechan.mapsee.place.constant;

/**
 * 장소 저장 상태
 * - TEMPORARY: AI 분석으로 추출된 임시 상태 (사용자가 저장 여부 미결정)
 * - SAVED: 사용자가 명시적으로 저장한 상태
 */
public enum PlaceSavedStatus {
  /**
   * 임시 저장 상태
   * - AI 분석 결과로 자동 생성됨
   * - 사용자가 아직 저장 여부를 결정하지 않음
   * - 삭제 가능
   */
  TEMPORARY,

  /**
   * 저장된 상태
   * - 사용자가 명시적으로 저장함
   * - 삭제 불가능 (임시 저장만 삭제 가능)
   */
  SAVED
}
