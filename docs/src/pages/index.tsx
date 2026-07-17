import type { ReactNode } from "react";
import clsx from "clsx";
import Link from "@docusaurus/Link";
import useDocusaurusContext from "@docusaurus/useDocusaurusContext";
import Layout from "@theme/Layout";
import styles from "./index.module.css";
import CodeBlock from "@theme/CodeBlock";
function HomepageHeader() {
  return (
    <div>
      <h1>flamme</h1>
      <h2>
        A quarkus extension for building Java distributed applications, without
        distributed complexity. Inspired by google's{" "}
        <span> service weaver</span>.
      </h2>
      <Link
        className={clsx(styles.italic, styles.hoverable, styles.ctaLink)}
        to="/docs/introduction/what-is-flamme"
      >
        Get Started →
      </Link>
    </div>
  );
}

export default function Home(): ReactNode {
  const { siteConfig } = useDocusaurusContext();
  const helloWorldExampleCode = `import com.amadeus.flamme.runtime.annotations.Flamme;
import com.amadeus.flamme.runtime.annotations.FlammeImpl;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import jakarta.inject.Inject;
import java.util.Map;

@Flamme(
    serviceName = "greeter",
    multiPayloadKeys = {
        @Flamme.MultiPayloadKey(id = GreeterService.NAME_KEY, type = StringValue.class),
        @Flamme.MultiPayloadKey(id = GreeterService.GREETING_KEY, type = StringValue.class)
    }
)
public interface GreeterService {
    String NAME_KEY = "name";
    String GREETING_KEY = "greeting";

    Map<String, Message> sayHello(Map<String, Message> payload);
}

@FlammeImpl
class GreeterServiceImpl implements GreeterService {
    @Override
    public Map<String, Message> sayHello(Map<String, Message> payload) {
        StringValue name = (StringValue) payload.get(NAME_KEY);
        return Map.of(
            GREETING_KEY,
            StringValue.of("Hello, " + name.getValue() + "!")
        );
    }
}

class GreeterResource {
    @Inject
    GreeterService greeterService;

    String greet() {
        Map<String, Message> response = greeterService.sayHello(
            Map.of(GreeterService.NAME_KEY, StringValue.of("World"))
        );

        return ((StringValue) response.get(GreeterService.GREETING_KEY)).getValue();
    }
}
`;

  const commandString = `# Run as distributed
# First Node hosts only the resource.
java -jar -Dflamme.services.greeter.remote=true -Dquarkus.http.port=8080 ./quarkus-run.jar

# Second Node hosts the flamme component and doesn't listen on http.
java -jar -Dflamme.services.greeter.remote=false -Dquarkus.http.host-enabled=false ./quarkus-run.jar

# Or, run everything together
java -jar ./quarkus-run.jar
`;

  const contributors = [
    { name: "sonofthecomet-ctrl", url: "https://github.com/sonofthecomet-ctrl" },

    { name: "jolebob", url: "https://github.com/jolebob" },
  ]
  return (
    <Layout
      title={`Hello from ${siteConfig.title}`}
      description="flamme, a single jar for a distributed application"
    >
      <div className={styles.container}>
        <div className={styles["vertical-container"]}>
          <HomepageHeader />
          <span className={styles.mono}>Hello, World 🌎</span>
          <CodeBlock language="java">{helloWorldExampleCode}</CodeBlock>
          <CodeBlock language="bash">{commandString}</CodeBlock>
        </div>
        <div>
          <div>Flamme is made possible by: </div>
          <div style={{ display: "flex", gap: 16 }}>
            {contributors.map((c) => (
              <a key={c.name} href={c.url} target="_blank" rel="noreferrer">
                <img
                  src={`https://github.com/${c.name}.png?size=96`}
                  alt={`${c.name} avatar`}
                  width={48}
                  height={48}
                  style={{ borderRadius: "50%" }}
                  loading="lazy"
                />
              </a>
            ))}
          </div>
        </div>
      </div>
    </Layout>
  );
}
