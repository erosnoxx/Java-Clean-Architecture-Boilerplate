# Java Clean Architecture Boilerplate

Backend Java com Clean Architecture e DDD, estruturado em 5 módulos Maven: `domain`, `application`, `infrastructure`, `web`, `boot`.

## Skills disponíveis

Carregue `jca-overview` antes de qualquer trabalho de implementação no projeto.

| Skill | Quando invocar |
|---|---|
| `jca-overview` | Antes de qualquer implementação — arquitetura, módulos, regras globais e roteamento para outros skills |
| `jca-add-domain` | Ao criar um novo bounded context do zero |
| `jca-add-value-object` | Ao criar um Value Object (string, decimal ou genérico) |
| `jca-add-entity` | Ao criar uma entidade de domínio |
| `jca-add-use-case` | Ao adicionar um use case a um domínio existente |
| `jca-add-state-machine` | Ao implementar State Machine em uma entidade com transições de estado |
| `jca-add-domain-event` | Ao criar um domain event e seu listener de infraestrutura |
| `jca-add-query-adapter` | Ao precisar de dados de outro bounded context sem violar isolamento |

## Regra de ouro

Nenhuma classe de domínio ou aplicação usa `@Component`, `@Service` ou `@Repository`. Todo wiring é manual em `boot/config/beans/`.
