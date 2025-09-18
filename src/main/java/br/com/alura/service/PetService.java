package br.com.alura.service;

import br.com.alura.client.ClientHttpConfiguration;
import br.com.alura.domain.Pet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class PetService {

    private final ClientHttpConfiguration client;

    public PetService(ClientHttpConfiguration client) {
        this.client = client;
    }

    public void listarPetsDoAbrigo() throws IOException, InterruptedException {
        System.out.println("Digite o id ou nome do abrigo:");
        String idOuNome = new Scanner(System.in).nextLine();

        String uri = "http://localhost:8080/abrigos/" + idOuNome + "/pets";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).method("GET", HttpRequest.BodyPublishers.noBody()).build();
        HttpResponse<String> response = client.dispararRequisicaoGet(uri);
        int statusCode = response.statusCode();
        if (statusCode == 404 || statusCode == 500) {
            System.out.println("ID ou nome não cadastrado!");
        }
        String responseBody = response.body();
        // Notas do curso: A intenção é utilizar o new ObjectMapper utilizando o método readValue(). Vamos passar o response.body como primeiro parâmetro e o segundo parâmetro será um array de Pet, Pet[]. Preciso definir, colocando .class, porque precisamos passar qual classe o Jackson utilizará como referência para executar essa conversão por baixo dos panos.
        Pet[] pets = new ObjectMapper().readValue(responseBody, Pet[].class);
        // Notas do curso: Mais uma vez, para evitar trabalharmos com o array, podemos converter para uma lista, já que temos mais métodos e flexibilidade ao trabalhar com uma lista do Java, com uma coleção do Java. Para fazer isso, utilizamos a API de streams que começou a ser vista no Java 8, muito famosa e útil para auxiliar nessa transformação.
        List<Pet> petList = Arrays.stream(pets).toList();
        System.out.println("Pets cadastrados:");
        for (Pet pet : petList) {
            long id = pet.getId();
            String tipo = pet.getTipo();
            String nome = pet.getNome();
            String raca = pet.getRaca();
            int idade = pet.getIdade();
            System.out.println(id + " - " + tipo + " - " + nome + " - " + raca + " - " + idade + " ano(s)");
        }
    }

    public void importarPetsDoAbrigo() throws IOException, InterruptedException {
//        String idOuNome = null;
//        String nomeArquivo = null;

        Scanner scanner = new Scanner(System.in);

        System.out.println("Digite o id ou nome do abrigo:");
//            idOuNome = new Scanner(System.in).nextLine();
        String idOuNome = scanner.nextLine();

        System.out.println("Digite o nome do arquivo CSV:");
//            nomeArquivo = new Scanner(System.in).nextLine();
        String nomeArquivo = scanner.nextLine();


        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(nomeArquivo));
        } catch (IOException e) {
            System.out.println("Erro ao carregar o arquivo: " + nomeArquivo);
        }
        String line;

        while ((line = reader.readLine()) != null) {
            String[] campos = line.split(",");
            String tipo = campos[0];
            String nome = campos[1];
            String raca = campos[2];
            int idade = Integer.parseInt(campos[3]);
            String cor = campos[4];
            Float peso = Float.parseFloat(campos[5]);

            Pet pet = new Pet(tipo, nome, raca, idade, cor, peso);

            String uri = "http://localhost:8080/abrigos/" + idOuNome + "/pets";

            HttpResponse<String> response = client.dispararRequisicaoPost(uri, pet);
            int statusCode = response.statusCode();
            String responseBody = response.body();
            if (statusCode == 200) {
                System.out.println("Pet cadastrado com sucesso: " + nome);
            } else if (statusCode == 404) {
                System.out.println("Id ou nome do abrigo não encontado!");
                break;
            } else if (statusCode == 400 || statusCode == 500) {
                System.out.println("Erro ao cadastrar o pet: " + nome);
                System.out.println(responseBody);
                break;
            }
        }

        reader.close();
    }
}
