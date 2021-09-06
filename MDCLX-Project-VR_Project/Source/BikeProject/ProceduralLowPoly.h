// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "EngineUtils.h"
#include "GameFramework/Actor.h"
#include "Components/StaticMeshComponent.h"
#include "ProceduralLowPoly.generated.h"

UCLASS()
class BIKEPROJECT_API AProceduralLowPoly : public AActor
{
	GENERATED_BODY()
	
public:	
	// Sets default values for this actor's properties
	AProceduralLowPoly();

	UPROPERTY(EditAnywhere, BlueprintReadWrite)
	int32 levels;

	UPROPERTY(EditAnywhere, BlueprintReadWrite)
	int32 windowsFront;

	UPROPERTY(EditAnywhere, BlueprintReadWrite)
	int32 windowsLateral;

protected:
	// Called when the game starts or when spawned
	virtual void BeginPlay() override;

public:	
	// Called every frame
	virtual void Tick(float DeltaTime) override;

};
