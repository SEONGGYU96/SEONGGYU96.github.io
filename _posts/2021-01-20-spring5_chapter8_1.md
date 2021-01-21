---
layout: post
title: "Spring5 : JdbcTemplate을 통한 DB 연동"
subtitle: "Spring5 study (8) - 1"
author: "GuGyu"
header-style: text
tags:
  - Spring
  - Spring5
  - Java
  - Study
  - Server
  - DB
  - Database
  - JDBC
---

많은 웹 어플리케이션은 데이터를 보관하기 위해 DBMS를 사용한다. DBMS와의 연동을 위해서, 자바에서는 JDBC API를 사용하거나 JPA, MyBatis와 같은 기술을 사용한다. 이 중에서 JDBC를 사용하기 위해 스프링이 제공하는 `JdbcTemplate`을 이용하여 DB와 연동 및 데이터 처리하는 방법에 대해 알아보자. 
<br>

## JDBC

JdbcTemplate을 알기 전에, 당연히 JDBC부터 먼저 알아야한다.  

JDBC란 **J**ava **D**ata**B**ase **C**onnectivity 의 약어로써 자바에서의 데이터베이스 연결을 의미한다. 어플리케이션에서 데이터베이스를 조작, 연결할 수 있게 해주는 API라고 할 수 있다.  

JDBC는 `Java.sql` 패키지에 작성되어 있으며 MySQL, 오라클 등 DBMS 종류에 의존하지 않아 동일한 코드를 여러 DBMS와의 연동에 사용할 수 있는 것이 특징이다.

DBMS의 종류에 의존하지 않을 수 있는 이유는 <b>JDBC 드라이버 매니저</b>덕분이다. JDBC 드라이버 매니저는 각각의 DBMS에 의존하고 있는 JDBC 드라이버들의 집합을 통합적으로 관리한다.   

<img src="https://user-images.githubusercontent.com/57310034/105009947-7cbbe580-5a7e-11eb-9a15-263653ccd70e.png"/>  
<br>

어플리케이션과 JDBC, DBMS 사이 인터페이스 관계를 **약식**으로 표현하자면 위 그림과 같다. 실제로는 JDBC Driver와 DBMS 사이에는 미들웨어도 존재하며 시스템, 접근 및 처리 방식 등 다양한 조건에 따라 드라이버 타입이 나뉜다. 아무튼 이처럼 우리가 JDBC API를 사용하면 JDBC 드라이버 매니저가 각 DBMS 드라이버에 맞는 JDBC 드라이버 API를 대신 사용해준다.  

### JDBC를 이용한 프로그래밍 방법

JDBC를 이용하여 DBMS와의 연동 및 데이터를 처리하기 위해서는 다음과 같은 절차가 필요하다.

1. 드라이버를 로드한다.
2. `Connection` 객체를 생성하여 DB에 접속한다.
3. `Statement` 객체를 생성하여 쿼리문을 실행한다.
4. 쿼리문에 결과물이 있다면 `ResultSet` 객체를 생성해 담아준다.
5. 어플리케이션에서 결과물을 사용한다.
6. 열었던 모든 객체들을 (RecultSet, Statement, Connection) 열었던 반대 순서대로 모두 닫아준다.

이 과정을 예제 코드로 살펴보자.  

```java
//(1)드라이버 로드
Class.forName("oracle.jdbc.driver.OracleDriver")
...

Member member;
Connection connection = null;
PreparedStatement preparedStatement = null;
ResultSet resultSet = null;

try {
    //(2)접속할 DB의 URL으로 Connection 객체를 생성
    connection = DriverManager.getConnection("jdbc:mysql://localhost/spring", "spring", "spring");

    //(3) 쿼리문으로 Statement 객체 생성
    preparedStatement = connection.prepareStatement("select * from MEMBER where EMAIL=?");

    //(3 - 1) 쿼리문의 변수 초기화
    preparedStatement.setString(1, email);

    //(4) 쿼리문의 결과물을 ResultSet객체를 생성해 담음
    resultSet = preparedStatement.executeQuery();

    //(5) 결과물 사용
    if (resultSet.next()) {
        member = new Member(resultSet.getString("EMAIL"),
            resultSet.getString("PASSWORD"),
            resultSet.getString("NAME"),
            resultSet.getTimestamp("REGDATE"),
        );
        member.setId(resultSet.getLong("ID"));
        return member;
    } else {
        return null;
    }
} catch (SQLException exception) {
    e.printStackTrace();
    throw exception;
} finally { //(6) 열었던 객체들을 모두 닫아줌
    if (resultSet != null) {
        try {
            resultSet.close();
        } catch (SQLException exception2) {
            ...
        } 
    }
    if (preparedStatement != null) {
        try {
            preparedStatement.close();
        } catch (SQLException exception3) {
            ...
        } 
    }
    if (connection != null) {
        try {
            connection.close();
        } catch (SQLException exception3) {
            ...
        } 
    }
}
```  

위 코드는 `select` 쿼리를 이용하여 Member에 대한 정보를 DBMS로 부터 받아온다. 단순한 작업인데도 생성해야하는 객체가 많고 이를 모두 닫아주기 위한 작업도 일일이 해주어야 한다. 잘 살펴보면 실제로 쿼리문을 작성하고 받아온 데이터를 처리하는 부분보다 객체들을 생성하고 닫아주는 코드가 더 길다. 프로젝트의 규모가 커질 수록 이런 보일러플레이트 코드는 늘어날 것이고 프로그래머들은 의미없는 노동을 반복해야한다.  

이런 점들을 개선하기 위해서 스프링은 `JdbcTemplate`를 제공하는 것이다.  

<br>

## JdbcTemplate

스프링은 JDBC API 사용에 있어서 반복적으로 발생하는 코드를 줄이기 위해 템플릿 메서드 패턴과 전략 패턴을 엮은 `JdbcTemplate`을 제공한다. 이를 이해하기 위해 먼저 템플릿 메서드 패턴과 전략 패턴을 간단히 알아보자.

### 템플릿 메서드 패턴 

<img src="https://user-images.githubusercontent.com/57310034/105027490-2b6a2100-5a93-11eb-95aa-57109a1afd02.png"/>  

전체적인 작업 수행 구조는 바꾸지 않으면서 작업을 처리하는 핵심 부분의 구현을 서브클래스에게 맡겨 전체적인 알고리즘 코드를 재사용할 수 있도록 설계하는 패턴이다. 즉, 전체적으로 동일한 부분은 상위 클래스에서 정의하고 확장/변화가 필요한 부분만 서브클래스에서 구현할 수 있도록 하는 것이다. 여기서 서브클래스가 구현할 핵심 부분의 메서드를 <b>primitive 메서드</b> 혹은 <b>hook 메서드</b>라고 한다.  

### 전략 패턴

OCP원칙의 위배를 피하면서 조금 더 유연한 기능의 확장을 위해, 객체들이 할 수 있는 각각의 행위에 대해 전략 클래스를 생성하고 객체의 행동에 수정이 필요할 때 전략 클래스만을 교체할 수 있도록 설계하는 패턴이다.  

우리는 추상화와 다형성을 활용하기 위해 객체의 공통적인 행위와 특성을 뽑아 추상 클래스나 인터페이스로 작성한다. 그리고 이들을 상속해 각 객체의 성격에 맞게 구현한다. 그러나 어떤 객체가 구현한 행동에 변화가 생기거나 확장이 필요할 때, 이를 직접 수정하게 되면 OCP 원칙에 위배된다.  

```java
public interface Movable {
    public void move();
}

public class AirPlane extends Movable {
    @Override
    public void move() {
        System.out.println("항공유를 태워 프로펠러를 회전시킨다.")
    }
}

public class Car extends Movable {
    @Override
    public void move() {
        System.out.println("휘발유를 태워 바퀴를 회전시킨다.")
    }
}
```  

위와 같이 움직이는 탈것에 대한 공통적인 행위인 `move()` 를 `Movable` 이라는 인터페이스에 추상화하였다. 이렇게 하면 어떤 탈것이든 간에 사람이 사용할 때는 해당 탈것의 `move()` 만 호출하면 움직일 수 있을 것이다. 하지만 만약 자동차의 연료가 전기로 바뀌면 어떻게 될까? 자동차의 `move()` 메서드를 직접 수정해야할 것이다. 하지만 이렇게 되면 OCP 원칙을 위배하게 된다. 그리고 전기 오토바이, 전기 자전거 등 다양한 객체들이 전기로 바퀴를 움직인다는 행위를 사용한다면 수 많은 중복 코드가 발생할 것이다. 따라서 움직임에 해당하는 "전략"을 분리하여 별도의 클래스로 작성할 수 있다.  

```java
public interface MovableStrategy {
    public void move();
}

public class GasolineBaseStrategy {
    @override
    public void move() {
        System.out.println("휘발유를 태워 바퀴를 회전시킨다.")
    }
}

public class ElectricityBaseStrategy {
    @Override
    public void move() {
        System.out.println("전기를 소모해 바퀴를 회전시킨다.")
    }
}
```

그럼 이제 이렇게 수정할 수 있다.

```java
public class Moving {
    private movableStrategy;

    Moving(MovableStrategy movableStrategy) {
        this.movableStrategy = movableStrategy;
    }

    public void move() {
        movableStrategy.move();
    }
}

public class Car extends Moving {
    Car(MovableStrategy movableStrategy) {
        super(movableStrategy)
    }
}

public static void main(String[] args) {
    //휘발유 자동차
    Moving gacolineCar = new Car(new GasolineBaseStrategy());

    //전기 자동차
    Moving electronicCar = new Car(new ElectricityBaseStrategy());
}
```

---

다시 JdbcTemplate으로 돌아와서 처음에 작성한 JDBC 예제 코드를 JdbcTemplate을 사용하여 수정해보겠다.  

```java
List<Member> results = jdbcTemplate.query(
    "select * from MEMBER where EMAIL=?",
    (ResultSet resultSet, int row) -> {
        Member member = new Member(resultSet.getString("EMAIL"),
            resultSet.getString("PASSWORD"),
            resultSet.getString("NAME"),
            resultSet.getTimestamp("REGDATE")
        );
        member.setId(resultSet.getLong("ID"));
        return member;
    }, email);
)
return results.isEmpty() ? null : results.get(0);
```

`jdbcTemplate.query(String sql, ResultSetExtractor<T> rse, Object... args)` 하나의 메서드로 아까 그 길던 코드를 대체할 수 있다. 프로그래머는 쿼리문과 매개변수, 데이터 처리 콜백만 작성하면 나머지 전체적인 알고리즘은 템플릿 코드가 처리한다.  

스프링의 jdbcTemplate 의 또 다른 장점은 트랜잭션 관리가 쉽다는 것이다. 기존 JDBC API로 트랜잭션을 처리하려면 `Connection#setAutoCommit(false)`를 이용해서 자동 커밋을 비활성화 한 다음, `commit()`과 `rollback()` 메서드를 이용해서 하나하나 트랜잭션을 작성하여야 한다.  

```java
public void insert(Memeber member) {
    Connection connection = null;
    PreparedStatement preparedStatement = null;

    try {
        connection = DriverManager.getConnection(
            "jdbc:mysql://localhost/spring?characterEncoding=utf8",
            "spring", "spring"
        );
        connection.setAutoCommit(false);
        ...(DB 쿼리 실행)...
        connection.commit()
    } catch {SQLException exception} {
        if (connection != null) {
            try { connection.rollback(); } catch (SQLException exeption1) {}
        }
    } finally { ... }
}
```

위와 같은 트랜잭션 정의 코드를 JdbcTemplate은 어노테이션 하나로 줄일 수 있다.

```java
@Transactional
public void insert(Member member) {
    ...
}
```

커밋과 롤백 처리는 스프링이 알아서 처리하므로 프로그래머는 트랜잭션 처리를 제외한 핵심 코드만 집중해서 작성하면 된다.  
<br>

## MySQL DBMS와 연동

### JdbcTemplate Dependency 추가

JdbcTemplate을 사용하려면 build.gradle 파일에 의존을 작성해주어야한다.
```gradle
dependencies {
    ...
    implementation "org.springframework:spring-context:$rootProject.springFrameworkVerison"

    //JdbcTemplate 등 JDBC 연동에 필요한 기능을 제공
    implementation "org.springframework:spring-jdbc:$rootProject.springFrameworkVerison"

    //DB 커넥션 풀 기능을 제공
    implementation "org.apache.tomcat:tomcat-jdbc:$rootProject.tomcatVersion"

    //MySQL 연결에 필요한 JDBC 드라이버를 제공
    implementation "mysql:mysql-connector-java:$rootProject.mysqlVersion"
}
```

<br>

### DB 테이블 생성

`Homebrew`로 MySQL 패키지를 설치해 초기화를 진행하고, DB 서버를 실행시킨다. 그 다음, IntelliJ와 MySQL 콘솔을 연동시키면 편리하다. 과정은 아래 링크를 참고하자.  
[MySQL 설치](https://whitepaek.tistory.com/16)  
[IntelliJ와 연동](https://mdwgti16.github.io/jsp/mysql/#)  

MySQL 서버에 다음과 같은 테이블을 생성하고 테스트 데이터를 삽입하였다.   

```sql
create database spring character set=utf8;

create table spring.MEMBER (
    ID int auto_increment primary key,
    EMAIL varchar(255),
    PASSWORD varchar(100),
    NAME varchar(100),
    REGDATE datetime,
    unique key (EMAIL)
) engine=InnoDB character set = utf8;
```
```sql
insert into MEMBER (EMAIL, PASSWORD, NAME, REGDATE)
values ('link5658@gmail.com', '1234', 'gugyu', now());
```  

<br>

### DataSource 설정

위 JDBC API 예시에서 DB와의 연결을 위해서 `DriverManager.getConnection()` 메서드를 사용했다. 하지만 이 방법 외에 `DataSource#getConnection()` 를 사용하는 방법도 있다.  

DataSource는 `javax.sql` 패키지에 내장된 인터페이스이며, 다음과 같이 DB 연결과 관련된 메서드들을 정의하고 있다.  

```java
public interface DataSource  extends CommonDataSource, Wrapper {

    //DB 연결
    Connection getConnection() throws SQLException;

    Connection getConnection(String username, String password) throws SQLException;

    //Log 관련
    @Override
    java.io.PrintWriter getLogWriter() throws SQLException;

    @Override
    void setLogWriter(java.io.PrintWriter out) throws SQLException;

    //Timeout 설정
    @Override
    void setLoginTimeout(int seconds) throws SQLException;

    @Override
    int getLoginTimeout() throws SQLException;

    default ConnectionBuilder createConnectionBuilder() throws SQLException {
        throw new SQLFeatureNotSupportedException(
            "createConnectionBuilder not implemented"
        );
    };
}
```

물론 이 인터페이스를 직접 구현할 필요는 없다. 다양한 구현 클래스들이 있는데, 우리는 커넥션 풀 기능을 지원하는 `Tomcat JDBC` 모듈에 포함된 클래스를 사용하고자 한다. 이 Tomcat의 DataSource에 DB 연결과 필요한 정보들을 초기화해주고 스프링 빈으로 등록하면 편리하게 사용할 수 있다.  

```java
//임포트에 주의
import org.apache.tomcat.jdbc.pool.DataSource;

@Configuration
public class AppContext {

    //빈 소멸 메서드 지정
	@Bean(destroyMethod = "close")
	public DataSource dataSource() {
		DataSource dataSource = new DataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://localhost/spring?characterEncoding=utf8");
		dataSource.setUsername("testUser");
		dataSource.setPassword("test");

        //커넥션 풀 초기 개수
		dataSource.setInitialSize(2);

        //최대 커넥션 유지 개수
		dataSource.setMaxActive(10);

        //Idle 상태의 커넥션을 검사하도록 설정
		dataSource.setTestWhileIdle(true);

        //해당 시간 이상 Idle 상태이면 커넥션 제거
		dataSource.setMinEvictableIdleTimeMillis(60000 * 3);

        //Idle 커넥션을 검사할 주기를 설정
		dataSource.setTimeBetweenEvictionRunsMillis(10 * 1000);
		return dataSource;
	}
}
```

위 설정은 커넥션 풀 생성 시 최초 2개의 커넥션을 미리 생성해두고, 최대 10개의 커넥션을 수용할 수 있도록 한 것이다. 그리고 10초마다 Idle 상태의 커넥션 (풀에 반환되어 있는 커넥션)을 검사하여 커넥션이 끊어졌는지 확인한다. 이때 3분 이상 Idle 상태인 커넥션을 발견하면 커넥션 풀에서 제거한다. DBMS에서 특정 시간 이상 쿼리를 실행하지 않으면 연결이 끊어지도록 설정할 수 있기 때문이다.  

추가로, Tomcat의 DataSource는 `close()` 메서드를 갖고 있는데 DataSource가 생성한 모든 커넥션을 제거하고 풀을 닫는 작업을 한다. 따라서 DataSource 빈 객체를 모두 사용하고 소멸할 때 해당 메서드를 실행시킬 수 있도록 `@Bean(destroyMethod = "close")`를 사용하였다.
<br>

## 쿼리 실행

이제 본격적으로 JdbcTemplate을 사용해 쿼리를 실행해보자.  
<br>

### JdbcTemplate 생성

```java
public class MemberDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public MemberDao(DataSource dataSource) {
        //DataSource로 JdbcTemplate 객체 생성
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
}
```
DataSource 객체는 빈으로 등록해주었기 때문에 MemeberDao를 생성할 때 자동으로 주입될 것이다. 그리고 이제 설정 클래스에 MemberDao를 등록해주자.

```java
@Configuration
public class AppContext {

    @Bean
    public DataSource dataSource() {
        ...
    }

    //MemberDao 빈 등록 
    @Bean
    public MemberDao memberDao() {
        return new MemberDao(dataSource());
    }
}
```  
<br>

### 조회 쿼리

#### Query()

JdbcTemplate 클래스는 `SELECT` 쿼리 실행을 위한 `query()` 메서드를 제공한다. 자주 사용되는 쿼리 메서드는 다음과 같다.

- `List<T> query(String sql, RowMapper<T> rowMapper)`
- `List<T> query(String sql, Object[] args, RowMapper<T> rowMapper)`
- `List<T> query(String sql, RowMapper<T> rowMapper, Object... args)`


`RowMapper<T>`는 `ResultSet` 형태의 결과 값을 자바 객체에 매핑할 수 있는 인터페이스를 제공한다.  

```java
@FunctionalInterface //람다로도 사용 가능
public interface RowMapper<T> {

	@Nullable
	T mapRow(ResultSet rs, int rowNum) throws SQLException;
}
```
따라서 `query()` 메서드를 실행할 때 해당 메서드를 구현해서 실행하면, 구현부에 따라 결과 값을 자바 객체로 매핑해준다.  

그리고 만약 파라미터를 가진 쿼리이면 args 파라미터를 이용해서 각 파라미터 값을 지정할 수 있다.  

```java
public class MemberDao {
    ...
    public Member selectByEmail(String email) {
        List<Member> results = jdbcTemplate.query(
                "select * from spring.MEMBER where EMAIL = ?",
                (ResultSet resultSet, int row) -> {
                    Member member = new Member(
                            resultSet.getString("EMAIL"),
                            resultSet.getString("PASSWORD"),
                            resultSet.getString("NAME"),
                            resultSet.getTimestamp("REGDATE").toLocalDateTime()
                    );
                    member.setId(resultSet.getLong("ID"));
                    return member;
                }, email);
        return results.isEmpty() ? null : results.get(0);
    }
}
```  
<br>

#### queryForObject()

`count()`이나 `sum()` 같은 집계 함수를 사용하는 쿼리의 경우, 결과 값은 하나이다. 따라서 List로 결과값을 얻기 보다는 Integer와 같은 정수 타입으로 받는 것이 더 편할 것이다. 물론 Integer 외에 다른 원하는 타입으로 결과 값을 얻고 싶을 때도 있을 것이다. 이럴 때 `queryForObject()` 메서드를 사용할 수 있다. 주요 메서드는 다음과 같다.

- `T queryForObject(String sql, Class<T> requiredType)`
- `T queryForObject(String sql, Class<T> requiredType, Object... args)`
- `T queryForObject(String sql, RowMapper<T> rowMapper)`
- `T queryForObject(String sql, RowMapper<T> rowMapper, Object... args)`

```java
public class MemberDao {
    public int count() {
        return jdbcTemplate.queryForObject(
                "select count(*) from spring.MEMBER",
                Integer.class
        );
    }
}
```   

하지만 `queryForObject()`를 사용하려면 쿼리의 실행 결과는 반드시 한 행이어야 한다. 만약 행이 없거나 두 개 이상이라면 예외를 발생시킨다. 따라서 한 행이 아닐 수도 있으면 반드시 `query()` 메서드를 사용하도록 하자.  

<br>

### 변경 쿼리

`Insert`, `UPDATE`, `DELETE` 쿼리는 `update()` 메서드를 사용한다.

- `int update(String sql)`
- `int update(String sql, Object... args)`
- `int update(PreparedStatementSetter pss)`

```java
public class MemberDao {
    ...
    public void update(Member member) {
        jdbcTemplate.update(
                "update spring.MEMBER set NAME = ?, PASSWORD = ? where EMAIL = ?",
                member.getName(),
                member.getPassword(),
                member.getEmail()
        );
    }
}
```

그런데 위 메서드들 중 `PreparedStatementSetter`타입을 파라미터로 하는 메서드가 있다. 이는 JDBC에서 사용하던 PreparedStatement를 통해 쿼리와 쿼리의 파라미터를 지정하는 방법을 사용하는 것이다.  

```java
public class MemberDao {
    ...
    public void insert(Member member) {
        jdbcTemplate.update((Connection connection) -> {
            PreparedStatement preparedStatement = connection.prepareStatement(
                "insert into spring.MEMBER (EMAIL, PASSWORD, NAME, REGDATE) value (?, ?, ?, ?)"
            );
            preparedStatement.setString(1, member.getEmail());
            preparedStatement.setString(2, member.getPassword());
            preparedStatement.setString(3, member.getName());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(member.getRegisterDateTime()));
            return preparedStatement;
        });
    }
}
```

<br>

#### KeyHolder  

MySQL의 AUTO_INCREMENT 칼럼은 행이 추가될 때 자동으로 값이 할당되는 칼럼으로서 primary key에 주로 사용된다.  

```sql
create table spring.MEMBER (
    ID int auto_increment primary key,
    ...
)
```
따라서 AUTO_INCREMENT와 같은 자동 증가 칼럼을 가진 테이블에 INSERT 쿼리를 사용할 경우 해당하는 값은 지정하지 않는다. 지금까지 작성한 예제도 마찬가지이다.  

그런데 쿼리 실행 후에 생성된 키 값을 알고 싶다면 어떻게 해야할까? 이때 `KeyHolder`를 사용할 수 있다.  

- `int update(final PreparedStatementCreator psc, final KeyHolder generatedKeyHolder)`  

```java
public class MemberDao {
    ...
    public void insert(Member member) {
        //GeneratedKeyHolder 객체 생성
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update((Connection connection) -> {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into spring.MEMBER (EMAIL, PASSWORD, NAME, REGDATE) value (?, ?, ?, ?)",

                    //preparedStatement의 파라미터로 ID 칼럼은
                    //자동 생성되는 칼럼임을 지목함
                    new String[]{"ID"}
            );
            ...
        }, keyHolder); //update 메서드의 파라미터로 keyHolder를 넘겨줌 

        //upddate 메서드 실행 중 자동 생성 칼럼으로 지정한 칼럼의 값을
        //keyholder에 담아줌
        Number keyValue = keyHolder.getKey();
        if (keyValue != null) {
            //키 값을 원하는 타입으로 언박싱해서 사용
            member.setId(keyValue.longValue());
        }
    }
}
```  

<br>





--- 
해당 포스팅은 [초보 웹 개발자를 위한 스프링5 프로그래밍 입문 - 최범균 저](https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=157472828)를 정리하며 작성하였습니다.

참고:  
JDBC :  
[https://m.blog.naver.com/PostView.nhn?blogId=yayatom&logNo=10114709858&proxyReferer=https:%2F%2Fwww.google.com%2F](https://m.blog.naver.com/PostView.nhn?blogId=yayatom&logNo=10114709858&proxyReferer=https:%2F%2Fwww.google.com%2F)  

템플릿 메서드 패턴 :  
[https://gmlwjd9405.github.io/2018/07/13/template-method-pattern.html](https://gmlwjd9405.github.io/2018/07/13/template-method-pattern.html)  

전략 패턴 :  
[https://victorydntmd.tistory.com/292](https://victorydntmd.tistory.com/292)

DataSource :  
[https://gmlwjd9405.github.io/2018/05/15/setting-for-db-programming.html](https://gmlwjd9405.github.io/2018/05/15/setting-for-db-programming.html)