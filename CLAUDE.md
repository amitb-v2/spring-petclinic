# spring-petclinic

## Requirement markers (`@lucigent`)

Code that implements a Jira requirement carries a `@lucigent` marker comment. The
Lucigent knowledge-graph ingester parses these to build
`requirement -> implemented_by -> code` edges, so **a missing marker means the work is
invisible to the graph**.

### When to add one

Add or update a marker whenever you write or change code in response to a requirement —
in practice, any time you used the `lucigent` MCP tools (`get_requirement_context`,
`search_graph`, `node_detail`, …) to understand what to build. Use the exact ticket key
you looked up.

This applies to new code *and* to edits of existing code: if you change a method to
satisfy a new ticket, append that ticket's key to the method's existing marker rather
than replacing it.

### Format

```
// @lucigent: <KEY>[, <KEY>...]
```

`<KEY>` is either a requirement key (`PC-5`) or one of its acceptance criteria
(`PC-5/AC-4`). Comma-separate when one unit serves several requirements — e.g.
`// @lucigent: PC-7, PC-15`.

`AC-<n>` refers to the criterion's position in the `acceptance_criteria` array returned
by `get_requirement_context`.

### Placement

**Method level** — a `//` comment directly above the method, above its annotations. This
is the default; prefer it, because it marks the narrowest unit that actually implements
the behaviour:

```java
// @lucigent: PC-5, PC-5/AC-4
@PostMapping("/owners/new")
public String processCreationForm(@Valid Owner owner, BindingResult result, ...) {
```

**Class level** — a `@lucigent:` tag inside the class Javadoc, only when the class as a
whole exists to serve that requirement:

```java
/**
 * @lucigent: PC-5
 * @author Juergen Hoeller
 */
@Controller
class OwnerController {
```

Do not put a class-level tag on a class that merely contains one tagged method.

### Working a ticket

1. Call `mcp__lucigent__get_requirement_context` with the ticket key first — it returns
   the description, the acceptance criteria, and any standing decisions that narrow the
   scope. Read the decisions; they often answer questions the description leaves open.
2. Implement, following the patterns of the nearest existing feature.
3. Tag each new or changed method with the ticket key, plus `<KEY>/AC-<n>` for the
   specific criteria that method satisfies.
4. Reference the ticket key in the commit message.
