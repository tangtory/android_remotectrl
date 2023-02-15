package com.lotte.mart.commonlib.exception

import java.io.PrintWriter
import java.io.StringWriter

/**
 * Exception 공통 처리 핸들러
 */
class ExceptionHandler {
    companion object {
        /**
         * Exception 공통 처리 수행
         * @param defaultValue - 예외 상황 발생 시 기본 리턴값
         * @param f - 수행 메소드
         * @return 수행 메소드의 리턴 값 또는 기본값
         */
        fun <T> tryOrDefault(defaultValue: T, f: () -> T): T {
            val sw = StringWriter()
            return try {
                f()
            } catch (e: ExceptionInInitializerError) {
                //static 변수가 초기화 되지 않고 사용될 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("ExceptionInInitializerError" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: ArrayIndexOutOfBoundsException) {
                //인덱스가 부 또는 배열의 사이즈 이상의 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("ArrayIndexOutOfBoundsException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: ArrayStoreException) {
                //부정한 형태의 객체를 객체의 배열에 포함하려는 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("ArrayStoreException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: ClassNotFoundException) {
                //클래스를 찾을 수 없는 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("ClassNotFoundException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: CloneNotSupportedException) {
                //객체를 복제 시 clone 메소드가 객체의 Cloneable 인터페이스를 구현하고 있지 않는 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("CloneNotSupportedException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: EnumConstantNotPresentException) {
                //지정된 이름의 정수를 가지지 않는 enum 형에 액세스 하려고 했을 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("EnumConstantNotPresentException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: IllegalAccessException) {
                //어플리케이션이, 배열 이외의 인스턴스 작성, 필드의 설정 또는 취득, 메서드의 호출을 시도했을 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("IllegalAccessException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: IllegalMonitorStateException) {
                //모니터를 가지지 않는 thread가 객체의 모니터로 기다리는 것을 시도한 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("IllegalMonitorStateException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: IllegalThreadStateException) {
                //thread 상태가 부적절한 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("IllegalThreadStateException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: InstantiationException) {
                //클래스가 인터페이스 또는 abstract 클래스이기 위해서 지정된 객체의 인스턴스를 생성할 수 없는 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("InstantiationException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: InterruptedException) {
                //thread가 오랫동안의 대기 상태, 휴지 상태, 또는 일시정지 상태일 때, 다른 thread가 Thread 클래스의 interrupt 메서드를 사용해 이 상태에 인터럽트를 걸었을 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("InterruptedException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: NegativeArraySizeException) {
                //음수 크기의 배열을 만들려고하는 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("NegativeArraySizeException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: NoSuchFieldException) {
                //지정된 이름의 필드가 클래스에는 없는 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("NoSuchFieldException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: NoSuchMethodException) {
                //특정의 메서드가 발견되지 않는 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("NoSuchMethodException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: ReflectiveOperationException) {
                //리플렉션과 관련된 예외
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("ReflectiveOperationException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: SecurityException) {
                //보안 관리자 검사가 실패할 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("SecurityException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: StringIndexOutOfBoundsException) {
                //문자열이 0보다 작거나 배열 크기보다 크거나 같은 값으로 인덱싱 될 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("StringIndexOutOfBoundsException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: TypeNotPresentException) {
                //프로그램이 유형 이름이 포함 된 문자열을 통해 클래스, 인터페이스, 열거 형 또는 주석 유형에 액세스하려고 시도하고 유형을 찾을 수 없을 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("TypeNotPresentException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: AccessDeniedException) {
                //일반적으로 파일 권한 또는 기타 액세스 확인으로 인해 파일 시스템 작업이 거부 될 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("AccessDeniedException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: ArithmeticException) {
                //예외적 인 산술 조건이 발생했을 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("ArithmeticException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: ClassCastException) {
                //어느 객체를 상속 관계에 없는 클래스에 캐스트 하려고 할 경우
                CommonException.doException("ClassCastException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: ConcurrentModificationException) {
                // 한 스레드가 컬렉션을 수정하는 동안 다른 스레드가 컬렉션을 반복하는 경우
                CommonException.doException("ConcurrentModificationException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: FileAlreadyExistsException) {
                //파일 또는 디렉터리를 만들려고 시도하고 해당 이름의 파일이 이미있는 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("FileAlreadyExistsException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: FileSystemException) {
                //하나 또는 두 개의 파일에서 파일 시스템 작업이 실패 할 때
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("FileSystemException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: IllegalArgumentException) {
                //부정한 인수, 또는 부적절한 인수를 메서드에 넘길 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("IllegalArgumentException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: IllegalStateException) {
                //부정 또는 부적절한 때에 메서드가 호출될 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("IllegalStateException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: IndexOutOfBoundsException) {
                // 인덱스가 범위를 벗어 났을 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("IndexOutOfBoundsException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: KotlinNullPointerException) {
                // 코틀린 값이 널일 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("KotlinNullPointerException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: NoSuchElementException) {
                //요청중인 요소가 존재하지 않을 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("NoSuchElementException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: NoSuchFileException) {
                //존재하지 않는 파일에 액세스하려고 할 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("NoSuchFileException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: NullPointerException) {
                //null인 객체를 사용하려고 할 때 발생
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("NullPointerException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: NumberFormatException) {
                //문자열을 숫자 유형 중 하나로 변환하려고 시도했지만 문자열에 적절한 형식이 없을 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("NumberFormatException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: RuntimeException) {
                //Java Virtual Machine의 정상 작동 중에 발생할 수있는 예외의 수퍼 클래스입니다.
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("RuntimeException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: TypeCastException) {
                //잘못된 유형의 캐스팅을 진행할 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("TypeCastException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: UninitializedPropertyAccessException) {
                //lateinit가 초기화 되지 않음
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("UninitializedPropertyAccessException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: UnsupportedOperationException) {
                //요청 된 작업이 지원되지 않을 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("UnsupportedOperationException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            } catch (e: Exception) {
                //이외 예외 상황 발생 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("Exception" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
                defaultValue
            }
        }

        /**
         * Exception 공통 처리 수행
         * @param f - 수행 메소드
         */
        fun <T> tryOrDefault(f: () -> T){
            val sw = StringWriter()
            try {
                f()
            } catch (e: ExceptionInInitializerError) {
                //static 변수가 초기화 되지 않고 사용될 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("ExceptionInInitializerError" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: ArrayIndexOutOfBoundsException) {
                //인덱스가 부 또는 배열의 사이즈 이상의 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("ArrayIndexOutOfBoundsException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: ArrayStoreException) {
                //부정한 형태의 객체를 객체의 배열에 포함하려는 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("ArrayStoreException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: ClassNotFoundException) {
                //클래스를 찾을 수 없는 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("ClassNotFoundException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: CloneNotSupportedException) {
                //객체를 복제 시 clone 메소드가 객체의 Cloneable 인터페이스를 구현하고 있지 않는 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("CloneNotSupportedException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: EnumConstantNotPresentException) {
                //지정된 이름의 정수를 가지지 않는 enum 형에 액세스 하려고 했을 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("EnumConstantNotPresentException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: IllegalAccessException) {
                //어플리케이션이, 배열 이외의 인스턴스 작성, 필드의 설정 또는 취득, 메서드의 호출을 시도했을 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("IllegalAccessException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: IllegalMonitorStateException) {
                //모니터를 가지지 않는 thread가 객체의 모니터로 기다리는 것을 시도한 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("IllegalMonitorStateException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: IllegalThreadStateException) {
                //thread 상태가 부적절한 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("IllegalThreadStateException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: InstantiationException) {
                //클래스가 인터페이스 또는 abstract 클래스이기 위해서 지정된 객체의 인스턴스를 생성할 수 없는 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("InstantiationException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: InterruptedException) {
                //thread가 오랫동안의 대기 상태, 휴지 상태, 또는 일시정지 상태일 때, 다른 thread가 Thread 클래스의 interrupt 메서드를 사용해 이 상태에 인터럽트를 걸었을 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("InterruptedException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: NegativeArraySizeException) {
                //음수 크기의 배열을 만들려고하는 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("NegativeArraySizeException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: NoSuchFieldException) {
                //지정된 이름의 필드가 클래스에는 없는 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("NoSuchFieldException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: NoSuchMethodException) {
                //특정의 메서드가 발견되지 않는 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("NoSuchMethodException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: ReflectiveOperationException) {
                //리플렉션과 관련된 예외
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("ReflectiveOperationException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: SecurityException) {
                //보안 관리자 검사가 실패할 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("SecurityException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: StringIndexOutOfBoundsException) {
                //문자열이 0보다 작거나 배열 크기보다 크거나 같은 값으로 인덱싱 될 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("StringIndexOutOfBoundsException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: TypeNotPresentException) {
                //프로그램이 유형 이름이 포함 된 문자열을 통해 클래스, 인터페이스, 열거 형 또는 주석 유형에 액세스하려고 시도하고 유형을 찾을 수 없을 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("TypeNotPresentException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: AccessDeniedException) {
                //일반적으로 파일 권한 또는 기타 액세스 확인으로 인해 파일 시스템 작업이 거부 될 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("AccessDeniedException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: ArithmeticException) {
                //예외적 인 산술 조건이 발생했을 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("ArithmeticException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: ClassCastException) {
                //어느 객체를 상속 관계에 없는 클래스에 캐스트 하려고 할 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("ClassCastException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: ConcurrentModificationException) {
                // 한 스레드가 컬렉션을 수정하는 동안 다른 스레드가 컬렉션을 반복하는 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("ConcurrentModificationException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: FileAlreadyExistsException) {
                //파일 또는 디렉터리를 만들려고 시도하고 해당 이름의 파일이 이미있는 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("FileAlreadyExistsException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: FileSystemException) {
                //하나 또는 두 개의 파일에서 파일 시스템 작업이 실패 할 때
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("FileSystemException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: IllegalArgumentException) {
                //부정한 인수, 또는 부적절한 인수를 메서드에 넘길 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("IllegalArgumentException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: IllegalStateException) {
                //부정 또는 부적절한 때에 메서드가 호출될 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("IllegalStateException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: IndexOutOfBoundsException) {
                // 인덱스가 범위를 벗어 났을 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("IndexOutOfBoundsException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: KotlinNullPointerException) {
                // 코틀린 값이 널일 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("KotlinNullPointerException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: NoSuchElementException) {
                //요청중인 요소가 존재하지 않을 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("NoSuchElementException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: NoSuchFileException) {
                //존재하지 않는 파일에 액세스하려고 할 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("NoSuchFileException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: NullPointerException) {
                //null인 객체를 사용하려고 할 때 발생
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("NullPointerException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: NumberFormatException) {
                //문자열을 숫자 유형 중 하나로 변환하려고 시도했지만 문자열에 적절한 형식이 없을 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("NumberFormatException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: RuntimeException) {
                //Java Virtual Machine의 정상 작동 중에 발생할 수있는 예외의 수퍼 클래스입니다.
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("RuntimeException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: TypeCastException) {
                //잘못된 유형의 캐스팅을 진행할 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("TypeCastException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: UninitializedPropertyAccessException) {
                //lateinit가 초기화 되지 않음
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("UninitializedPropertyAccessException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: UnsupportedOperationException) {
                //요청 된 작업이 지원되지 않을 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("UnsupportedOperationException" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            } catch (e: Exception) {
                //이외 예외 상황 발생 경우
                e.printStackTrace(PrintWriter(sw))
                CommonException.doException("Exception" + e.message, sw.toString(), CommonException.LEVEL_DEBUG)
            }
        }
    }
}