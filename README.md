# Java Clean Architecture Boilerplate

Boilerplate de backend Java com **Clean Architecture** e **DDD**, pronto para ser usado como base de novos projetos. Estruturado em 5 módulos Maven com separação total de responsabilidades, wiring manual no módulo `boot`, e um domínio `auth` completo como referência de implementação.

---

## Tecnologias

- **Java 21** + **Spring Boot 3.4.5**
- **PostgreSQL** (via Spring Data JPA + Hibernate)
- **JWT** para autenticação
- **MapStruct** para mapeamento de DTOs
- **Lombok** para redução de boilerplate
- **JUnit 5** + **AssertJ** para testes

---

## Arquitetura

O projeto segue Clean Architecture com DDD em 5 módulos Maven independentes. A dependência só flui de fora para dentro:

```
web → application → domain
infrastructure → application → domain
boot → tudo
```

### Os 5 Módulos

| Módulo | Responsabilidade | Regra principal |
|---|---|---|
| `domain` | Entidades, VOs, eventos, enums, state machines | Puro Java — sem Spring, sem infra |
| `application` | Interfaces de use cases e repositórios, critérios, schemas, mappers | Sem `@Component`, `@Service` |
| `infrastructure` | JPA entities, repositories, adapters, listeners | Spring-aware; acessa qualquer JPA repo |
| `web` | Controllers REST, exception handler | `@RestController` apenas |
| `boot` | **Composition root** — todos os `@Bean` e `@Configuration` | Nenhuma outra classe cria bean |

### Regra de Ouro

> Nenhuma classe em `domain` ou `application` usa `@Component`, `@Service` ou `@Repository`. Todo wiring é manual em `boot/config/beans/`.

Isso garante que o domínio seja 100% testável sem Spring context.

---

## Estrutura de Pacotes

```
com.boilerplate/
├── domain/
│   ├── common/                         # Bases reutilizáveis
│   │   ├── entities/DomainEntity.java
│   │   ├── vos/ValueObject.java
│   │   ├── vos/StringValueObject.java
│   │   ├── vos/BigDecimalValueObject.java
│   │   ├── events/DomainEvent.java
│   │   ├── exceptions/DomainException.java
│   │   └── statemachine/StateMachine.java
│   ├── shared/vos/Email.java           # VOs compartilhados entre domínios
│   └── auth/                           # Domínio de autenticação (referência)
│       ├── entities/User.java
│       ├── vos/{FullName, Password}.java
│       ├── enums/{UserStatus, UserRole}.java
│       ├── events/{UserCreated, UserActivated, ...}Event.java
│       ├── states/{UserState, Active, Inactive, Suspended}UserState.java
│       └── statemachine/UserStateMachine.java
│
├── application/
│   ├── common/                         # Ports e utilitários compartilhados
│   │   ├── repository/Repository.java
│   │   ├── annotations/CriteriaField.java
│   │   ├── pagination/{Page, Pageable}.java
│   │   ├── events/DomainEventPublisher.java
│   │   ├── utils/QueryUtils.java
│   │   └── sequence/DocumentSequencePort.java
│   └── auth/
│       ├── contracts/repositories/UserRepository.java
│       ├── contracts/usecases/         # Interfaces de use cases
│       ├── criteria/UserCriteria.java
│       ├── mappers/UserMapper.java
│       ├── schemas/{request, response}/
│       └── usecases/                   # Implementações
│
├── infrastructure/
│   ├── common/                         # Infraestrutura compartilhada
│   │   ├── mapper/EntityMapper.java
│   │   ├── persistence/entities/BaseJpaEntity.java
│   │   ├── persistence/repositories/RepositoryImpl.java
│   │   ├── persistence/repositories/CriteriaUtils.java
│   │   ├── publishers/DomainEventPublisherImpl.java
│   │   └── utils/{EventLoggingUtils, TimeConfig}.java
│   └── auth/
│       ├── data/entities/UserEntity.java
│       ├── data/jpa/UserJpaRepository.java
│       ├── mappers/UserEntityMapper.java
│       ├── repositories/UserRepositoryImpl.java
│       ├── listeners/UserEventListener.java
│       └── adapters/{Authentication, PasswordEncoder, TokenProvider}Adapter.java
│
├── web/
│   ├── controllers/{Auth, User}Controller.java
│   ├── middlewares/RestExceptionHandler.java
│   └── security/{AdminOnly, AuthenticatedOnly}.java
│
└── boot/
    ├── BootApplication.java
    ├── common/seeder/
    ├── config/{async, cors, security}/
    └── config/beans/
        ├── application/{ApplicationAuthBeans, MapperBeans}.java
        └── infra/{InfraRepository, InfraMapper, InfraListener, ...}Beans.java
```

---

## Começando um Novo Projeto

### 1. Clone o repositório

```bash
git clone https://github.com/erosnoxx/Java-Clean-Architecture-Boilerplate.git meu-projeto
cd meu-projeto
```

### 2. Renomeie os pacotes

Substitua `com.boilerplate` pelo seu group ID em todos os arquivos Java, `pom.xml` e diretórios:

```bash
# Exemplo com IntelliJ: Refactor → Rename na raiz do pacote com.boilerplate
# Ou via sed (Linux/Mac):
find . -type f \( -name "*.java" -o -name "*.xml" -o -name "*.properties" \) \
  -exec sed -i 's/com\.boilerplate/com.meuapp/g' {} +
```

Renomeie também os diretórios físicos de `boilerplate` para `meuapp`:
```
src/main/java/com/boilerplate/ → src/main/java/com/meuapp/
```

### 3. Configure o banco de dados

Edite `boot/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/meu_banco
spring.datasource.username=postgres
spring.datasource.password=senha
spring.jpa.hibernate.ddl-auto=update
```

### 4. Configure o JWT

```properties
jwt.secret=sua-chave-secreta-aqui-minimo-256-bits
jwt.expiration=86400000
jwt.refresh-expiration=604800000
```

### 5. Rode o projeto

```bash
./mvnw spring-boot:run -pl boot
```

O admin padrão é criado automaticamente pelo `AdminSeeder`. Verifique as credenciais em `boot/config/seeder/AdminSeeder.java`.

---

## Criando um Novo Domínio (Bounded Context)

Vamos criar um domínio `produto` como exemplo completo. O mesmo passo a passo serve para qualquer domínio novo.

> **Dica:** Use as [skills do Claude](/.claude/skills/) para auxiliar nesse processo. Carregue `jca-overview` no início de qualquer sessão de implementação.

---

### Passo 1 — Value Objects (módulo `domain`)

Crie um VO para cada campo que precisa de validação. Não use `String` cru nas entidades.

**`domain/src/main/java/com/meuapp/produto/vos/NomeProduto.java`**
```java
public final class NomeProduto extends StringValueObject {
    private static final int MIN = 2, MAX = 100;
    private NomeProduto(String value) { super(value); }
    public static NomeProduto of(String value) { return new NomeProduto(value); }
    @Override
    protected void customValidate(String value) { checkLength(value, MIN, MAX); }
}
```

**`domain/src/main/java/com/meuapp/produto/vos/Preco.java`**
```java
public final class Preco extends BigDecimalValueObject {
    private Preco(BigDecimal value) { super(value); }
    public static Preco of(BigDecimal value) { return new Preco(value); }
    @Override protected int scale() { return 2; }
    @Override protected RoundingMode roundingMode() { return RoundingMode.HALF_EVEN; }
    @Override protected String type() { return "preço"; }
    @Override protected boolean allowNegative() { return false; }
}
```

**Bases disponíveis:**

| Base | Quando usar |
|---|---|
| `StringValueObject` | Nome, código, descrição — qualquer String com regras |
| `BigDecimalValueObject` | Preço, quantidade, taxa — decimais com escala e arredondamento |
| `ValueObject<T>` | Qualquer outro tipo (UUID wrapper, Integer com regras) |

---

### Passo 2 — Status Enum com State Machine (módulo `domain`)

Se o domínio tiver estados (rascunho, ativo, descontinuado), use o padrão State Machine em vez de booleans.

**`domain/src/main/java/com/meuapp/produto/enums/ProdutoStatus.java`**
```java
public enum ProdutoStatus {
    ATIVO {
        @Override public ProdutoState getState() { return AtivoProdutoState.INSTANCE; }
    },
    INATIVO {
        @Override public ProdutoState getState() { return InativoProdutoState.INSTANCE; }
    };
    public abstract ProdutoState getState();
}
```

**`domain/src/main/java/com/meuapp/produto/states/ProdutoState.java`**
```java
public interface ProdutoState {
    void ativar(ProdutoStateMachine sm);
    void desativar(ProdutoStateMachine sm);
}
```

**`domain/src/main/java/com/meuapp/produto/states/AtivoProdutoState.java`**
```java
public class AtivoProdutoState implements ProdutoState {
    public static final AtivoProdutoState INSTANCE = new AtivoProdutoState();
    private AtivoProdutoState() {}

    @Override public void ativar(ProdutoStateMachine sm) {
        throw new DomainException("produto já está ativo");
    }
    @Override public void desativar(ProdutoStateMachine sm) {
        sm.transition(ProdutoStatus.INATIVO, new ProdutoDesativadoEvent(sm.getProdutoId()));
    }
}
```

**`domain/src/main/java/com/meuapp/produto/statemachine/ProdutoStateMachine.java`**
```java
public class ProdutoStateMachine extends StateMachine<ProdutoStatus> {
    private final UUID produtoId;

    private ProdutoStateMachine(ProdutoStatus status, UUID produtoId) {
        this.value = status;
        this.produtoId = produtoId;
    }

    public static ProdutoStateMachine of(Produto produto) {
        return new ProdutoStateMachine(produto.getStatus(), produto.getId());
    }

    public UUID getProdutoId() { return produtoId; }

    public void transition(ProdutoStatus novoStatus, DomainEvent evento) {
        this.value = novoStatus;
        this.event = evento;
    }

    public void ativar()   { value.getState().ativar(this); }
    public void desativar() { value.getState().desativar(this); }
}
```

---

### Passo 3 — Entidade de Domínio (módulo `domain`)

**`domain/src/main/java/com/meuapp/produto/entities/Produto.java`**
```java
@Getter
public class Produto extends DomainEntity<UUID> {

    private NomeProduto nome;
    private Preco preco;
    private ProdutoStatus status;

    private Produto() {}

    // create() — dispara eventos, aplica regras iniciais
    public static Produto create(NomeProduto nome, Preco preco) {
        var produto = new Produto();
        produto.setId(UUID.randomUUID());
        produto.nome = nome;
        produto.preco = preco;
        produto.status = ProdutoStatus.ATIVO;
        produto.registerEvent(new ProdutoCriadoEvent(produto.getId()));
        return produto;
    }

    // reconstitute() — restaura do banco, SEM eventos, SEM validação
    public static Produto reconstitute(UUID id, NomeProduto nome, Preco preco,
                                       ProdutoStatus status,
                                       OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        var produto = new Produto();
        produto.setId(id);
        produto.nome = nome;
        produto.preco = preco;
        produto.status = status;
        produto.setTimestamps(createdAt, updatedAt);
        return produto;
    }

    // Mutações via métodos de domínio — nunca via setter direto
    public void renomear(NomeProduto novoNome) { this.nome = novoNome; }
    public void reajustarPreco(Preco novoPreco) { this.preco = novoPreco; }

    public void ativar()    { transition(ProdutoStateMachine::ativar); }
    public void desativar() { transition(ProdutoStateMachine::desativar); }
    public boolean isAtivo() { return status == ProdutoStatus.ATIVO; }

    private void transition(Consumer<ProdutoStateMachine> acao) {
        var sm = ProdutoStateMachine.of(this);
        acao.accept(sm);
        this.status = sm.getValue();
        var evento = sm.getEvent();
        if (evento != null) registerEvent(evento);
    }
}
```

**Regras obrigatórias da entidade:**
- Construtor `private`
- `create()` para criação com regras de negócio e eventos
- `reconstitute()` para restauração do banco — sem `registerEvent()`
- `@Getter` na classe, **nunca** `@Setter`
- Mutações via métodos explícitos

---

### Passo 4 — Domain Events (módulo `domain`)

```java
// domain/produto/events/
public record ProdutoCriadoEvent(UUID produtoId) implements DomainEvent {}
public record ProdutoDesativadoEvent(UUID produtoId) implements DomainEvent {}
```

---

### Passo 5 — Contrato do Repositório e Critério (módulo `application`)

**`application/src/main/java/com/meuapp/produto/contracts/repositories/ProdutoRepository.java`**
```java
public interface ProdutoRepository extends Repository<Produto, UUID, ProdutoCriteria> {}
```

**`application/src/main/java/com/meuapp/produto/criteria/ProdutoCriteria.java`**
```java
public record ProdutoCriteria(
    @CriteriaField(value = "nome", operator = Operator.LIKE) String nome,
    @CriteriaField(value = "status", operator = Operator.EQ) ProdutoStatus status
) implements Criteria {}
```

`@CriteriaField` gera predicados JPA automaticamente via `CriteriaUtils`. Campos `null` são ignorados.

---

### Passo 6 — Use Cases (módulo `application`)

**Interface:** `application/.../contracts/usecases/CriarProdutoUseCase.java`
```java
public interface CriarProdutoUseCase {
    ProdutoResponse execute(CriarProdutoRequest request);
}
```

**Implementação:** `application/.../usecases/CriarProdutoUseCaseImpl.java`
```java
@RequiredArgsConstructor
public class CriarProdutoUseCaseImpl implements CriarProdutoUseCase {

    private final ProdutoRepository repository;
    private final DomainEventPublisher publisher;
    private final ProdutoMapper mapper;

    @Override
    public ProdutoResponse execute(CriarProdutoRequest request) {
        var produto = Produto.create(
            NomeProduto.of(request.nome()),
            Preco.of(request.preco())
        );

        var salvo = repository.save(produto);
        salvo.pullEvents().forEach(publisher::publish); // sempre depois do save

        return mapper.toResponse(salvo);
    }
}
```

**Para busca por ID, use `QueryUtils` em vez de `orElseThrow` inline:**
```java
var produto = QueryUtils.findOrThrow(repository, id, "Produto não encontrado");
```

**Schemas:**
```java
// request
public record CriarProdutoRequest(@NotBlank String nome, @NotNull BigDecimal preco) {}

// response
public record ProdutoResponse(UUID id, String nome, BigDecimal preco, ProdutoStatus status,
                               OffsetDateTime createdAt, OffsetDateTime updatedAt) {}
```

---

### Passo 7 — JPA Entity e Mapper (módulo `infrastructure`)

**`infrastructure/.../data/entities/ProdutoEntity.java`**
```java
@Entity @Table(name = "produtos")
@Getter @Setter @NoArgsConstructor
public class ProdutoEntity extends BaseJpaEntity<UUID> {

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProdutoStatus status;
}
```

**`infrastructure/.../mappers/ProdutoEntityMapper.java`**
```java
public class ProdutoEntityMapper implements EntityMapper<Produto, ProdutoEntity> {

    @Override
    public ProdutoEntity toPersistence(Produto domain) {
        var entity = new ProdutoEntity();
        entity.setId(domain.getId());
        entity.setNome(domain.getNome().getValue());     // desempacota VO → primitivo
        entity.setPreco(domain.getPreco().getValue());
        entity.setStatus(domain.getStatus());
        return entity;
    }

    @Override
    public Produto toDomain(ProdutoEntity persistence) {
        return Produto.reconstitute(
            persistence.getId(),
            NomeProduto.of(persistence.getNome()),       // reempacota primitivo → VO
            Preco.of(persistence.getPreco()),
            persistence.getStatus(),
            persistence.getCreatedAt(),
            persistence.getUpdatedAt()
        );
    }
}
```

**`infrastructure/.../repositories/ProdutoRepositoryImpl.java`**
```java
public class ProdutoRepositoryImpl
    extends RepositoryImpl<Produto, UUID, ProdutoCriteria, ProdutoEntity, ProdutoJpaRepository>
    implements ProdutoRepository {

    public ProdutoRepositoryImpl(ProdutoJpaRepository jpa,
                                  EntityManager em,
                                  ProdutoEntityMapper mapper) {
        super(jpa, em, mapper, ProdutoEntity.class);
    }
}
```

---

### Passo 8 — Event Listener (módulo `infrastructure`)

**`infrastructure/.../listeners/ProdutoEventListener.java`**
```java
@Slf4j
public class ProdutoEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("eventExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(ProdutoCriadoEvent event) {
        EventLoggingUtils.logEventStart(event, "produtoId=" + event.produtoId());
        // enviar e-mail, atualizar read model, notificar serviço externo...
        EventLoggingUtils.logEventEnd(event);
    }
}
```

As três anotações são obrigatórias juntas:
- `@TransactionalEventListener(AFTER_COMMIT)` — só processa se a transação principal commitou
- `@Async("eventExecutor")` — executa em thread separada (pool `eventExecutor` já configurado em `AsyncConfig`)
- `@Transactional(REQUIRES_NEW)` — listener tem sua própria transação isolada

---

### Passo 9 — Controller (módulo `web`)

**`web/.../controllers/ProdutoController.java`**
```java
@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final CriarProdutoUseCase criarProdutoUseCase;
    private final ListarProdutosUseCase listarProdutosUseCase;
    private final ProdutoMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoResponse criar(@RequestBody @Valid CriarProdutoRequest request) {
        return criarProdutoUseCase.execute(request);
    }

    @GetMapping
    public Page<ProdutoResponse> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return listarProdutosUseCase.execute(new ProdutoCriteria(nome, null), Pageable.of(page, size));
    }
}
```

---

### Passo 10 — Wiring no Boot (módulo `boot`)

Este é o passo mais importante. **Sem o wiring no boot, nenhuma classe será instanciada.**

**`boot/.../config/beans/application/ProdutoBeans.java`**
```java
@Configuration
@RequiredArgsConstructor
public class ProdutoBeans {
    private final ProdutoRepository produtoRepository;
    private final DomainEventPublisher publisher;
    private final ProdutoMapper produtoMapper;

    @Bean
    public CriarProdutoUseCase criarProdutoUseCase() {
        return new CriarProdutoUseCaseImpl(produtoRepository, publisher, produtoMapper);
    }

    @Bean
    public ListarProdutosUseCase listarProdutosUseCase() {
        return new ListarProdutosUseCaseImpl(produtoRepository, produtoMapper);
    }
}
```

**Adicione em `InfraRepositoryBeans.java`:**
```java
@Bean
public ProdutoRepository produtoRepository(ProdutoJpaRepository jpa,
                                             EntityManager em,
                                             ProdutoEntityMapper mapper) {
    return new ProdutoRepositoryImpl(jpa, em, mapper);
}
```

**Adicione em `InfraMapperBeans.java`:**
```java
@Bean
public ProdutoEntityMapper produtoEntityMapper() {
    return new ProdutoEntityMapper();
}
```

**Adicione em `MapperBeans.java`:**
```java
@Bean
public ProdutoMapper produtoMapper() {
    return Mappers.getMapper(ProdutoMapper.class);
}
```

**Adicione em `InfraListenerBeans.java`:**
```java
@Bean
public ProdutoEventListener produtoEventListener() {
    return new ProdutoEventListener();
}
```

---

## Acessando Dados de Outro Domínio

Nunca importe uma entidade de domínio de outro bounded context. Use o padrão **Query Adapter**.

Exemplo: domínio `pedido` precisa exibir o nome do usuário.

**1. DTO no domínio consumidor** — `application/pedido/queries/UsuarioSummary.java`
```java
public record UsuarioSummary(UUID id, String nome, String email) {}
```

**2. Port no domínio consumidor** — `application/pedido/queries/UsuarioQueryPort.java`
```java
public interface UsuarioQueryPort {
    Optional<UsuarioSummary> findById(UUID id);
}
```

**3. Adapter em infra** — `infrastructure/pedido/adapters/UsuarioQueryAdapter.java`
```java
@RequiredArgsConstructor
public class UsuarioQueryAdapter implements UsuarioQueryPort {
    private final UserJpaRepository userJpaRepository; // repo do domínio auth

    @Override
    public Optional<UsuarioSummary> findById(UUID id) {
        return userJpaRepository.findById(id)
            .map(e -> new UsuarioSummary(e.getId(), e.getName(), e.getEmail()));
    }
}
```

**4. Wiring no boot:**
```java
@Bean
public UsuarioQueryPort usuarioQueryPort(UserJpaRepository jpa) {
    return new UsuarioQueryAdapter(jpa);
}
```

O use case de `pedido` injeta `UsuarioQueryPort` (a interface), nunca o adapter diretamente.

---

## Paginação e Filtros

Todos os repositórios herdam `findAll(C criteria, Pageable pageable)`:

```java
// Criando o critério
var criteria = new ProdutoCriteria("teclado", ProdutoStatus.ATIVO);
var pageable = Pageable.of(0, 20, "nome", "ASC");

// No use case
Page<ProdutoResponse> page = repository.findAll(criteria, pageable)
    .map(mapper::toResponse);
```

Campos com `@CriteriaField` no record geram predicados automaticamente. Campos `null` são ignorados.

```java
public record ProdutoCriteria(
    @CriteriaField(value = "nome", operator = Operator.LIKE) String nome,
    @CriteriaField(value = "status", operator = Operator.EQ) ProdutoStatus status
) implements Criteria {}
```

---

## Numeração Sequencial de Documentos

Para gerar IDs sequenciais do tipo `PED-260703-0001`:

```java
@RequiredArgsConstructor
public class CriarPedidoUseCaseImpl implements CriarPedidoUseCase {
    private final DocumentSequencePort sequencePort;
    private final PedidoRepository repository;

    @Override
    public PedidoResponse execute(CriarPedidoRequest request) {
        var numeroPedido = sequencePort.next("PED"); // PED-260703-0001
        var pedido = Pedido.create(numeroPedido, ...);
        return mapper.toResponse(repository.save(pedido));
    }
}
```

A sequência reseta diariamente. O `DocumentSequenceAdapter` usa pessimistic lock para garantir unicidade em ambiente concorrente.

---

## Tratamento de Exceções

Exceções de domínio são mapeadas para HTTP automaticamente pelo `RestExceptionHandler`:

| Exceção | HTTP |
|---|---|
| `DomainException` | 400 Bad Request |
| `NotFoundException` | 404 Not Found |
| `ConflictException` | 409 Conflict |
| `ExternalServiceException` | 503 Service Unavailable |

Use a exceção correta no domínio:
```java
if (repository.findByEmail(email).isPresent())
    throw new ConflictException("E-mail já cadastrado");

var produto = QueryUtils.findOrThrow(repository, id, "Produto não encontrado");
```

---

## Testes

Os testes de domínio rodam sem Spring context — instanciação direta:

```java
class ProdutoTest {

    @Test
    void deveCriarProdutoComStatusAtivo() {
        var produto = Produto.create(NomeProduto.of("Teclado"), Preco.of(new BigDecimal("299.90")));
        assertThat(produto.getStatus()).isEqualTo(ProdutoStatus.ATIVO);
        assertThat(produto.getId()).isNotNull();
    }

    @Test
    void devePubilcarEventoAoCriar() {
        var produto = Produto.create(NomeProduto.of("Mouse"), Preco.of(new BigDecimal("99.90")));
        assertThat(produto.pullEvents()).hasSize(1)
            .first().isInstanceOf(ProdutoCriadoEvent.class);
    }

    @Test
    void naoDevePermitirDesativarProdutoJaInativo() {
        var produto = Produto.create(NomeProduto.of("Monitor"), Preco.of(new BigDecimal("1299.90")));
        produto.desativar();
        assertThatThrownBy(produto::desativar).isInstanceOf(DomainException.class);
    }
}
```

Execute todos os testes:
```bash
./mvnw test
```

---

## Skills do Claude

Este projeto inclui skills do Claude Code para auxiliar na implementação. Ao abrir o projeto em uma sessão Claude, o `CLAUDE.md` carrega automaticamente o índice de skills disponíveis.

| Skill | Quando usar |
|---|---|
| `jca-overview` | Início de qualquer sessão de implementação |
| `jca-add-domain` | Criar um novo bounded context do zero |
| `jca-add-value-object` | Criar um Value Object |
| `jca-add-entity` | Criar uma entidade de domínio |
| `jca-add-use-case` | Adicionar um use case |
| `jca-add-state-machine` | Implementar transições de estado |
| `jca-add-domain-event` | Criar domain event e listener |
| `jca-add-query-adapter` | Acessar dados de outro bounded context |

Para invocar: `@jca-overview` (ou conforme a sintaxe do seu cliente Claude).
