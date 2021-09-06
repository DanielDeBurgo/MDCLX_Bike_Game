// Fill out your copyright notice in the Description page of Project Settings.

#include "ProceduralLowPoly.h"
#include "EngineGlobals.h"
#include "Runtime/Engine/Classes/Engine/Engine.h"

// Sets default values
AProceduralLowPoly::AProceduralLowPoly()
{
 	// Set this actor to call Tick() every frame.  You can turn this off to improve performance if you don't need it.
	PrimaryActorTick.bCanEverTick = false;

}

// Called when the game starts or when spawned
void AProceduralLowPoly::BeginPlay()
{
	Super::BeginPlay();
	
	RootComponent = CreateDefaultSubobject<USceneComponent>("RootComponent");

	for (int32 i = 0; i < this->levels; i++)
	{
		UStaticMeshComponent* levelComponent = CreateDefaultSubobject<UStaticMeshComponent>("Level" + i);
		static ConstructorHelpers::FObjectFinder<UStaticMesh> levelBlock(TEXT("/Game/StarterContent/Shapes/Shape_Cube"));

		levelComponent->OnComponentCreated();
		levelComponent->RegisterComponent();
		levelComponent->SetupAttachment(RootComponent);
		levelComponent->SetStaticMesh(levelBlock.Object);

		FVector location = GetActorLocation();
		location.Z = i * 30;

		levelComponent->SetRelativeLocation(location);


	}	

}

// Called every frame
void AProceduralLowPoly::Tick(float DeltaTime)
{
	Super::Tick(DeltaTime);

}

