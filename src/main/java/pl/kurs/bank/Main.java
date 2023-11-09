package pl.kurs.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class);
    }

    // Analiza błędu ze stanem konta:
    // Najbardziej prawdopodobny problem dotyczy równoczesnych transakcji na koncie. Transakcje,
    // które wpływają jednocześnie na stan konta mogą prowadzić do takiego problemu ponieważ w trakcie jednoczesnego działania, każda z nich "widzi",
    // że na koncie są wystarczające środki do przeprowadzenia transakcji. W tym wypadku działając jednocześnie obie mogą zatwierdzić wypłatę środków i
    // doprowadzić do ujemnego stanu konta.

    //Wykonano:
    // 1) Aby wyeliminować błąd, który mógłby pozwolić na wypłatę gdy saldo jest ujemne zmodyfikowano metode withdraw w AccountService. Również
    //    aby wyeliminować komplikacje w metodzie deposit w AccountService wprowadzono zmiany, sprawdzające czy kwota depozytu jest wartością dodatnią.
    // 2) Dodano endpoint getAccount w AccountController, w tym celu dodano metode getAccount w klasie AccountService z której korzystamy, w ten sposób zwróci
    //    nam konto z podanym przez nas id.
    // 3) Wykonano testy jednostkowe dla locka i unlocka konta w klasie AccountControllerTest
    // 4) Zastosowano transakcje zablokowane pesymistycznie w repozytorium, wykonano metodę findByIdWithPessimisticLock którą kolejną zaimplementowałem
    //    w withdraw i deposit aby miala wpływ na transakcje. Zastosowanie tego, pozwoliło na uniknięcie konfliktów z racji na współbieżny dostęp do konta
    //    podczas gdy przeprowadzane są na nim jednocześnie wpłaty i wypłaty środków. Każda transkacja ma wyłączny dostęp do konta na czas swojego trwania.
    //    Przeprowadzono odpowiednie testy w Apache JMeter, które potwierdzają prawidłowe działanie metod aplikacji.
}