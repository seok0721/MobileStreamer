package localhost.webrtc;

/**
 * 피어 커넥션 타입을 나타냅니다.
 */
public enum PeerConnectionType {
	/**
	 * 요청자 타입
	 */
	Offerer,
	/**
	 * 응답자 타입
	 */
	Answerer,
	/**
	 * 대기자 타입
	 */
	Waiter
}